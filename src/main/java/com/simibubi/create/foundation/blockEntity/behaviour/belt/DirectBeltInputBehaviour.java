package com.simibubi.create.foundation.blockEntity.behaviour.belt;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Behaviour for BlockEntities to which belts can transfer items directly in a
 * backup-friendly manner. Example uses: Basin, Saw, Depot
 */
public class DirectBeltInputBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<DirectBeltInputBehaviour> TYPE = new BehaviourType<>();

	private InsertionCallback tryInsert;
	private AvailabilityPredicate canInsert;
	private Supplier<Boolean> supportsBeltFunnels;

	public DirectBeltInputBehaviour(SmartBlockEntity be) {
		super(be);
		tryInsert = this::defaultInsertionCallback;
		canInsert = d -> true;
		supportsBeltFunnels = () -> false;
	}

	public DirectBeltInputBehaviour allowingBeltFunnelsWhen(Supplier<Boolean> pred) {
		supportsBeltFunnels = pred;
		return this;
	}

	public DirectBeltInputBehaviour allowingBeltFunnels() {
		supportsBeltFunnels = () -> true;
		return this;
	}

	public DirectBeltInputBehaviour onlyInsertWhen(AvailabilityPredicate pred) {
		canInsert = pred;
		return this;
	}

	public DirectBeltInputBehaviour setInsertionHandler(InsertionCallback callback) {
		tryInsert = callback;
		return this;
	}

	private ItemStack defaultInsertionCallback(TransportedItemStack inserted, Direction side, boolean simulate) {
		LazyOptional<IItemHandler> lazy = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lazy.isPresent())
			return inserted.stack;
		return ItemHandlerHelper.insertItemStacked(lazy.orElse(null), inserted.stack.copy(), simulate);
	}

	// TODO: verify that this side is consistent across all calls
	public boolean canInsertFromSide(Direction side) {
		return canInsert.test(side);
	}

	public ItemStack handleInsertion(ItemStack stack, Direction side, boolean simulate) {
		return handleInsertion(new TransportedItemStack(stack), side, simulate);
	}

	public ItemStack handleInsertion(TransportedItemStack stack, Direction side, boolean simulate) {
		return tryInsert.apply(stack, side, simulate);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@FunctionalInterface
	public interface InsertionCallback {
		public ItemStack apply(TransportedItemStack stack, Direction side, boolean simulate);
	}

	@FunctionalInterface
	public interface AvailabilityPredicate {
		public boolean test(Direction side);
	}

	@Nullable
	public ItemStack tryExportingToBeltFunnel(ItemStack stack, @Nullable Direction side, boolean simulate) {
		BlockPos funnelPos = blockEntity.getBlockPos()
			.above();
		Level world = getWorld();
		BlockState funnelState = world.getBlockState(funnelPos);
		if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.SHAPE) != Shape.PULLING)
			return null;
		if (side != null && FunnelBlock.getFunnelFacing(funnelState) != side)
			return null;
		BlockEntity be = world.getBlockEntity(funnelPos);
		if (!(be instanceof FunnelBlockEntity))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.POWERED))
			return stack;
		ItemStack insert = FunnelBlock.tryInsert(world, funnelPos, stack, simulate);
		if (insert.getCount() != stack.getCount() && !simulate)
			((FunnelBlockEntity) be).flap(true);
		return insert;
	}

	public boolean canSupportBeltFunnels() {
		return supportsBeltFunnels.get();
	}

}
