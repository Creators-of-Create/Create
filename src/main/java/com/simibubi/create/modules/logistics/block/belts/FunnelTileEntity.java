package com.simibubi.create.modules.logistics.block.belts;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InventoryManagementBehaviour.Attachments;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class FunnelTileEntity extends SmartTileEntity {

	private static FilteringBehaviour.SlotPositioning slots;
	private FilteringBehaviour filtering;
	private InsertingBehaviour inserting;
	private ItemStack justEaten;

	public FunnelTileEntity() {
		super(AllTileEntities.BELT_FUNNEL.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			createSlotPositioning();
		filtering = new FilteringBehaviour(this).withCallback(this::filterChanged).withSlotPositioning(slots);
		behaviours.add(filtering);
		inserting = new InsertingBehaviour(this,
				Attachments.toward(() -> AttachedLogisticalBlock.getBlockFacing(getBlockState())));
		behaviours.add(inserting);
	}

	public void filterChanged(ItemStack stack) {
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		if (justEaten != null) {
			tag.put("Nom", justEaten.serializeNBT());
			justEaten = null;
		}
		return super.writeToClient(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		if (tag.contains("Nom"))
			justEaten = ItemStack.read(tag.getCompound("Nom"));
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote && justEaten != null) {
			spawnParticles(justEaten);
			justEaten = null;
		}
	}

	public ItemStack tryToInsert(ItemStack stack) {
		if (!filtering.test(stack))
			return stack;

		ItemStack remainder = ItemStack.EMPTY;

		BeltTileEntity targetingBelt = getTargetingBelt();
		if (targetingBelt != null) {
			Direction facing = AttachedLogisticalBlock.getBlockFacing(getBlockState());
			if (!targetingBelt.tryInsertingFromSide(facing, stack.copy(), false))
				remainder = stack;
		} else {
			remainder = inserting.insert(stack.copy(), false);
		}

		if (remainder.isEmpty()) {
			if (!world.isRemote)
				world.playSound(null, pos, AllSoundEvents.BLOCK_FUNNEL_EAT.get(), SoundCategory.BLOCKS, .125f, 1f);
			justEaten = stack.copy();
		}

		if (remainder.getCount() != stack.getCount())
			sendData();

		return remainder;
	}

	protected BeltTileEntity getTargetingBelt() {
		BlockPos targetPos = pos.offset(AttachedLogisticalBlock.getBlockFacing(getBlockState()));
		if (!AllBlocks.BELT.typeOf(world.getBlockState(targetPos)))
			return null;
		TileEntity te = world.getTileEntity(targetPos);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public void spawnParticles(ItemStack stack) {
		Vec3i directionVec = getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getDirectionVec();
		float xSpeed = directionVec.getX() * 1 / 8f;
		float zSpeed = directionVec.getZ() * 1 / 8f;
		Vec3d vec = VecHelper.getCenterOf(pos);
		world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vec.x, vec.y - 9 / 16f, vec.z, xSpeed,
				1 / 6f, zSpeed);
	}

	protected void createSlotPositioning() {
		slots = new SlotPositioning(state -> {
			Vec3d offsetForHorizontal = VecHelper.voxelSpace(8f, 14f, 13.5f);
			Vec3d offsetForBelt = VecHelper.voxelSpace(8f, 8.5f, 14f);
			Vec3d offsetForUpward = VecHelper.voxelSpace(8f, 13.5f, 2f);
			Vec3d offsetForDownward = VecHelper.voxelSpace(8f, 2.5f, 2f);
			Vec3d vec = offsetForHorizontal;

			float yRot = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));
			if (AttachedLogisticalBlock.isVertical(state))
				vec = state.get(AttachedLogisticalBlock.UPWARD) ? offsetForUpward : offsetForDownward;
			else if (state.get(FunnelBlock.BELT))
				vec = offsetForBelt;

			return VecHelper.rotateCentered(vec, yRot, Axis.Y);

		}, state -> {
			Direction blockFacing = AttachedLogisticalBlock.getBlockFacing(state);
			boolean vertical = AttachedLogisticalBlock.isVertical(state);
			float horizontalAngle = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));

			float yRot = blockFacing == Direction.DOWN ? horizontalAngle + 180 : horizontalAngle;
			float zRot = (vertical || state.get(FunnelBlock.BELT)) ? 90 : 0;

			if (blockFacing == Direction.UP)
				zRot += 180;

			return new Vec3d(0, yRot, zRot);
		}).scale(.4f);
	}

}
