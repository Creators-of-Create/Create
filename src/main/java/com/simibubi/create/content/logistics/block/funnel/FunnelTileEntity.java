package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class FunnelTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour invManipulation;

	int sendFlap;
	InterpolatedChasingValue flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, BELT
	}

	public FunnelTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		flap = new InterpolatedChasingValue().start(.25f)
			.target(0)
			.withSpeed(.05f);
	}

	public Mode determineCurrentMode() {
		BlockState state = getBlockState();
		if (!FunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.has(BlockStateProperties.POWERED) && state.get(BlockStateProperties.POWERED))
			return Mode.PAUSED;
		if (state.getBlock() instanceof BeltFunnelBlock)
			return Mode.BELT;
		return Mode.COLLECT;
	}

	@Override
	public void tick() {
		super.tick();
		Mode mode = determineCurrentMode();
		if (mode == Mode.BELT)
			tickAsBeltFunnel();
		if (world.isRemote)
			return;
	}

	public void tickAsBeltFunnel() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.get(BeltFunnelBlock.HORIZONTAL_FACING);
		flap.tick();
		if (world.isRemote)
			return;

		Boolean pushing = blockState.get(BeltFunnelBlock.PUSHING);
		if (!pushing) {
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

		ItemStack stack = invManipulation.extract(invManipulation.getAmountFromFilter(),
			s -> inputBehaviour.handleInsertion(s, facing, true)
				.isEmpty());
		if (stack.isEmpty())
			return;
		flap(false);
		inputBehaviour.handleInsertion(stack, facing, false);
	}

	private TransportedResult collectFromHandler(TransportedItemStack stack) {
		TransportedResult ignore = TransportedResult.doNothing();
		ItemStack toInsert = stack.stack.copy();
		if (!filtering.test(toInsert))
			return ignore;
		ItemStack remainder = invManipulation.insert(toInsert);
		if (remainder.equals(stack.stack, false))
			return ignore;

		flap(true);

		if (remainder.isEmpty())
			return TransportedResult.removeItem();
		TransportedItemStack changed = stack.copy();
		changed.stack = remainder;
		return TransportedResult.convertTo(changed);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		invManipulation = new InvManipulationBehaviour(this, InterfaceProvider.oppositeOfBlockFacing());
		behaviours.add(invManipulation);

		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning()).showCountWhen(() -> {
			BlockState blockState = getBlockState();
			return blockState.getBlock() instanceof HorizontalInteractionFunnelBlock
				&& blockState.get(HorizontalInteractionFunnelBlock.PUSHING);
		});
		filtering.onlyActiveWhen(this::supportsFiltering);
		behaviours.add(filtering);
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
			.setInsertionHandler(this::handleDirectBeltInput));
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		return blockState != null && blockState.getBlock() instanceof FunnelBlock
			&& blockState.get(FunnelBlock.FACING) == Direction.UP;
	}

	private boolean supportsFiltering() {
		BlockState blockState = getBlockState();
		return blockState != null && blockState.has(BlockStateProperties.POWERED);
	}

	private ItemStack handleDirectBeltInput(TransportedItemStack stack, Direction side, boolean simulate) {
		ItemStack inserted = stack.stack;
		if (!filtering.test(inserted))
			return inserted;
		if (determineCurrentMode() == Mode.PAUSED)
			return inserted;
		if (simulate)
			invManipulation.simulate();
		return invManipulation.insert(inserted);
	}

	public void flap(boolean inward) {
		sendFlap = inward ? 1 : -1;
		sendData();
	}

	public boolean hasFlap() {
		return getBlockState().getBlock() instanceof BeltFunnelBlock
			&& getBlockState().get(BeltFunnelBlock.SHAPE) == Shape.RETRACTED;
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (clientPacket && sendFlap != 0) {
			compound.putInt("Flap", sendFlap);
			sendFlap = 0;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket && compound.contains("Flap")) {
			int direction = compound.getInt("Flap");
			flap.set(direction);
		}
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return hasFlap() ? super.getMaxRenderDistanceSquared() : 64;
	}

}
