package com.simibubi.create.content.logistics.block.mechanicalArm;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.realityFunnel.RealityFunnelBlock;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class ArmInteractionPoint {

	static enum Mode {
		DEPOSIT, TAKE
	}

	BlockPos pos;
	BlockState state;
	Mode mode;

	private LazyOptional<IItemHandler> cachedHandler;
	private ArmAngleTarget cachedAngles;

	public ArmInteractionPoint() {
		cachedHandler = LazyOptional.empty();
	}

	@OnlyIn(Dist.CLIENT)
	void transformFlag(MatrixStack stack) {}

	AllBlockPartials getFlagType() {
		return mode == Mode.TAKE ? AllBlockPartials.FLAG_LONG_OUT : AllBlockPartials.FLAG_LONG_IN;
	}

	void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	Vec3d getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	abstract boolean isValid(BlockState state);

	static boolean isInteractable(BlockState state) {
		return AllBlocks.DEPOT.has(state) || AllBlocks.BELT.has(state) || AllBlocks.CHUTE.has(state)
			|| state.getBlock() instanceof RealityFunnelBlock;
	}

	ArmAngleTarget getTargetAngles(BlockPos armPos) {
		if (cachedAngles == null)
			cachedAngles = new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection());
		return cachedAngles;
	}

	@Nullable
	IItemHandler getHandler(World world) {
		if (!cachedHandler.isPresent()) {
			TileEntity te = world.getTileEntity(pos);
			if (te == null)
				return null;
			cachedHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		}
		return cachedHandler.orElse(null);
	}

	ItemStack insert(World world, ItemStack stack, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return stack;
		return ItemHandlerHelper.insertItem(handler, stack, simulate);
	}

	ItemStack extract(World world, int slot, int amount, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return ItemStack.EMPTY;
		return handler.extractItem(slot, amount, simulate);
	}

	ItemStack extract(World world, int slot, boolean simulate) {
		return extract(world, slot, 64, simulate);
	}

	int getSlotCount(World world) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return 0;
		return handler.getSlots();
	}

	@Nullable
	static ArmInteractionPoint createAt(IBlockReader world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ArmInteractionPoint point = null;

		if (AllBlocks.DEPOT.has(state))
			point = new Depot();
		if (AllBlocks.BELT.has(state))
			point = new Belt();
		if (AllBlocks.CHUTE.has(state))
			point = new Chute();
		if (state.getBlock() instanceof RealityFunnelBlock)
			point = new Funnel();

		if (point != null) {
			point.state = state;
			point.pos = pos;
			point.mode = Mode.DEPOSIT;
		}

		return point;
	}

	CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.put("Pos", NBTUtil.writeBlockPos(pos));
		NBTHelper.writeEnum(nbt, "Mode", mode);
		return nbt;
	}

	static ArmInteractionPoint deserialize(IBlockReader world, CompoundNBT nbt) {
		BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
		ArmInteractionPoint interactionPoint = createAt(world, pos);
		if (interactionPoint == null)
			return null;
		interactionPoint.mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
		return interactionPoint;
	}

	static class Depot extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return new Vec3d(pos).add(.5f, 14 / 16f, .5f);
		}

		@Override
		boolean isValid(BlockState state) {
			return AllBlocks.DEPOT.has(state);
		}

	}

	static class Belt extends Depot {

		@Override
		boolean isValid(BlockState state) {
			return AllBlocks.BELT.has(state);
		}
	}

	static class Chute extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return new Vec3d(pos).add(.5f, 1, .5f);
		}

		@Override
		boolean isValid(BlockState state) {
			return AllBlocks.CHUTE.has(state);
		}
	}

	static class Funnel extends ArmInteractionPoint {

		@Override
		Vec3d getInteractionPositionVector() {
			return VecHelper.getCenterOf(pos)
				.add(new Vec3d(RealityFunnelBlock.getFunnelFacing(state)
					.getDirectionVec()).scale(.5f));
		}

		@Override
		int getSlotCount(World world) {
			return 0;
		}

		@Override
		ItemStack extract(World world, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		Direction getInteractionDirection() {
			return RealityFunnelBlock.getFunnelFacing(state)
				.getOpposite();
		}

		@Override
		ItemStack insert(World world, ItemStack stack, boolean simulate) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
			InsertingBehaviour inserter = TileEntityBehaviour.get(world, pos, InsertingBehaviour.TYPE);
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			return inserter.insert(stack, simulate);
		}

		@Override
		boolean isValid(BlockState state) {
			return state.getBlock() instanceof RealityFunnelBlock;
		}

		@Override
		void cycleMode() {}

	}

}
