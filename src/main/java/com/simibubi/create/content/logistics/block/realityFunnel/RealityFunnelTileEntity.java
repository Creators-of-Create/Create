package com.simibubi.create.content.logistics.block.realityFunnel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.ExtractingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InventoryManagementBehaviour.Attachments;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RealityFunnelTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	private InsertingBehaviour inserting;
	private ExtractingBehaviour extracting;

	static enum Mode {
		INVALID, PAUSED, COLLECT, BELT, CHUTE_SIDE, CHUTE_END
	}

	public RealityFunnelTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public Mode determineCurrentMode() {
		BlockState state = getBlockState();
		if (!RealityFunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.get(BlockStateProperties.POWERED))
			return Mode.PAUSED;
		if (AllBlocks.BELT_FUNNEL.has(state))
			return Mode.BELT;
		if (AllBlocks.CHUTE_FUNNEL.has(state))
			return Mode.CHUTE_SIDE;

		Direction facing = RealityFunnelBlock.getFunnelFacing(state);
		BlockState input = world.getBlockState(pos.offset(facing));

		if (AllBlocks.CHUTE.has(input))
			return Mode.CHUTE_END;
		return Mode.COLLECT;
	}

	@Override
	public void tick() {
		super.tick();
		Mode mode = determineCurrentMode();
		if (mode == Mode.BELT)
			tickAsBeltFunnel();
	}

	public void tickAsBeltFunnel() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.get(BeltFunnelBlock.HORIZONTAL_FACING);
		if (world.isRemote)
			return;

		if (!blockState.get(BeltFunnelBlock.PUSHING)) {
			// Belts handle insertion from their side
			if (AllBlocks.BELT.has(world.getBlockState(pos.down())))
				return;
			TransportedItemStackHandlerBehaviour handler =
				TileEntityBehaviour.get(world, pos.down(), TransportedItemStackHandlerBehaviour.TYPE);
			if (handler == null)
				return;
			handler.handleCenteredProcessingOnAllItems(1 / 32f, this::collectFromHandler);
			return;
		}

		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		if (!inputBehaviour.canInsertFromSide(facing))
			return;

		extracting.setCallback(stack -> inputBehaviour.handleInsertion(stack, facing, false));
		extracting.withAdditionalFilter(stack -> inputBehaviour.handleInsertion(stack, facing, true)
			.isEmpty());
		extracting.extract();
	}

	private List<TransportedItemStack> collectFromHandler(TransportedItemStack stack) {
		ItemStack toInsert = stack.stack.copy();
		if (!filtering.test(toInsert))
			return null;
		ItemStack remainder = inserting.insert(toInsert, false);
		if (remainder.equals(stack.stack, false))
			return null;
		List<TransportedItemStack> list = new ArrayList<>();
		if (remainder.isEmpty())
			return list;
		TransportedItemStack changed = stack.copy();
		changed.stack = remainder;
		list.add(changed);
		return list;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		Supplier<List<Pair<BlockPos, Direction>>> direction =
			Attachments.toward(() -> RealityFunnelBlock.getFunnelFacing(getBlockState())
				.getOpposite());

		inserting = new InsertingBehaviour(this, direction);
		extracting = new ExtractingBehaviour(this, direction);
		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());

		behaviours.add(filtering);
		behaviours.add(inserting);
		behaviours.add(extracting);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 64;
	}

}
