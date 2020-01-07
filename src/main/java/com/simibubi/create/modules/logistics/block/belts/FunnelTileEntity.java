package com.simibubi.create.modules.logistics.block.belts;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.ItemHandlerBeltSegment;
import com.simibubi.create.modules.logistics.block.IInventoryManipulator;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelTileEntity extends SmartTileEntity implements ITickableTileEntity, IInventoryManipulator {

	private static FilteringBehaviour.SlotPositioning slots;
	private FilteringBehaviour filtering;

	private LazyOptional<IItemHandler> inventory;
	protected boolean waitingForInventorySpace;
	private ItemStack justEaten;

	public FunnelTileEntity() {
		super(AllTileEntities.BELT_FUNNEL.type);
		inventory = LazyOptional.empty();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			createSlotPositioning();
		filtering = new FilteringBehaviour(this).withCallback(this::filterChanged).withSlotPositioning(slots);
		behaviours.add(filtering);
	}

	public void filterChanged(ItemStack stack) {
		neighborChanged();
	}

	@Override
	public void read(CompoundNBT compound) {
		waitingForInventorySpace = compound.getBoolean("Waiting");
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Waiting", waitingForInventorySpace);
		return super.write(compound);
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
		if (!waitingForInventorySpace)
			neighborChanged();
		if (tag.contains("Nom"))
			justEaten = ItemStack.read(tag.getCompound("Nom"));
	}

	@Override
	public BlockPos getInventoryPos() {
		return pos.offset(getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

	@Override
	public LazyOptional<IItemHandler> getInventory() {
		return inventory;
	}

	@Override
	public void tick() {
		if (world.isRemote && justEaten != null) {
			spawnParticles(justEaten);
			justEaten = null;
		}
	}

	@Override
	public void initialize() {
		neighborChanged();
		super.initialize();
	}

	@Override
	public void setInventory(LazyOptional<IItemHandler> inventory) {
		this.inventory = inventory;
	}

	@Override
	public void neighborChanged() {
		IInventoryManipulator.super.neighborChanged();
		waitingForInventorySpace = false;
		if (!world.isRemote)
			sendData();
	}

	public ItemStack tryToInsert(ItemStack stack) {
		if (!inventory.isPresent())
			return stack;
		if (waitingForInventorySpace && !(inventory.orElse(null) instanceof ItemHandlerBeltSegment))
			return stack;
		if (!filtering.test(stack))
			return stack;

		IItemHandler inv = inventory.orElse(null);
		ItemStack inserted = stack.copy();
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(inv, inserted, false);
		waitingForInventorySpace = true;

		if (remainder.isEmpty()) {
			if (!world.isRemote)
				world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, .125f, 1f);
			justEaten = stack.copy();
			waitingForInventorySpace = false;
		}

		sendData();
		return remainder;
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
