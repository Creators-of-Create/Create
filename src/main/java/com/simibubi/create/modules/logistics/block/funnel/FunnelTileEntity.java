package com.simibubi.create.modules.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InventoryManagementBehaviour.Attachments;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.AttachedLogisticalBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class FunnelTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	private InsertingBehaviour inserting;
	private ItemStack justEaten;

	public FunnelTileEntity() {
		super(AllTileEntities.BELT_FUNNEL.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FunnelFilterSlot()).withCallback(this::filterChanged);
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
		if (!AllBlocksNew.BELT.has(world.getBlockState(targetPos)))
			return null;
		return BeltHelper.getSegmentTE(world, targetPos);
	}

	public void spawnParticles(ItemStack stack) {
		Vec3i directionVec = getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getDirectionVec();
		float xSpeed = directionVec.getX() * 1 / 8f;
		float zSpeed = directionVec.getZ() * 1 / 8f;
		Vec3d vec = VecHelper.getCenterOf(pos);
		world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vec.x, vec.y - 9 / 16f, vec.z, xSpeed,
				1 / 6f, zSpeed);
	}

}
