package com.simibubi.create.foundation.tileEntity.behaviour.belt;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Behaviour for TileEntities to which belts can transfer items directly in a
 * backup-friendly manner. Example uses: Basin, Saw, Depot
 */
public class DirectBeltInputBehaviour extends TileEntityBehaviour {

	public static final BehaviourType<DirectBeltInputBehaviour> TYPE = new BehaviourType<>();

	private InsertionCallback tryInsert;
	private AvailabilityPredicate canInsert;
	private Supplier<Boolean> supportsBeltFunnels;

	public DirectBeltInputBehaviour(SmartTileEntity te) {
		super(te);
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
		Storage<ItemVariant> storage = TransferUtil.getItemStorage(tileEntity, side);
		if (storage == null)
			return inserted.stack;

		try (Transaction t = TransferUtil.getTransaction()) {
			long trying = inserted.stack.getCount();
			long successful = storage.insert(ItemVariant.of(inserted.stack), inserted.stack.getCount(), t);
			if (trying == successful) {
				if (!simulate) t.commit();
				return ItemStack.EMPTY;
			}
			ItemStack stack = inserted.stack.copy();
			stack.setCount((int) (trying - successful));
			if (!simulate) t.commit();
			return stack;
		}
	}

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
		BlockPos funnelPos = tileEntity.getBlockPos()
			.above();
		Level world = getWorld();
		BlockState funnelState = world.getBlockState(funnelPos);
		if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.SHAPE) != Shape.PULLING)
			return null;
		if (side != null && FunnelBlock.getFunnelFacing(funnelState) != side)
			return null;
		BlockEntity te = world.getBlockEntity(funnelPos);
		if (!(te instanceof FunnelTileEntity))
			return null;
		if (funnelState.getValue(BeltFunnelBlock.POWERED))
			return stack;
		ItemStack insert = FunnelBlock.tryInsert(world, funnelPos, stack, simulate);
		if (insert.getCount() != stack.getCount() && !simulate)
			((FunnelTileEntity) te).flap(true);
		return insert;
	}

	public boolean canSupportBeltFunnels() {
		return supportsBeltFunnels.get();
	}

}
