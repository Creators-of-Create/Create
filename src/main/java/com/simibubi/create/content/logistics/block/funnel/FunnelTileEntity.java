package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.config.AllConfigs;
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
	private InvManipulationBehaviour autoExtractor;
	private int extractionCooldown;

	int sendFlap;
	InterpolatedChasingValue flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, PUSHING_TO_BELT, TAKING_FROM_BELT, HOPPER
	}

	public FunnelTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		extractionCooldown = 0;
		flap = new InterpolatedChasingValue().start(.25f)
			.target(0)
			.withSpeed(.05f);
	}

	public Mode determineCurrentMode() {
		BlockState state = getBlockState();
		if (!FunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.method_28500(BlockStateProperties.POWERED).orElse(false))
			return Mode.PAUSED;
		if (FunnelBlock.getFunnelFacing(state) == Direction.UP && autoExtractor.hasInventory())
			return Mode.HOPPER;
		if (state.getBlock() instanceof BeltFunnelBlock) {
			boolean pushing = state.get(BeltFunnelBlock.PUSHING);
			return pushing ? Mode.PUSHING_TO_BELT : Mode.TAKING_FROM_BELT;
		}
		return Mode.COLLECT;
	}

	@Override
	public void tick() {
		super.tick();
		flap.tick();
		Mode mode = determineCurrentMode();
		if (world.isRemote)
			return;

		// Redstone resets the extraction cooldown
		if (mode == Mode.PAUSED)
			extractionCooldown = 0;
		if (mode == Mode.TAKING_FROM_BELT)
			tickAsPullingBeltFunnel();

		if (extractionCooldown > 0) {
			extractionCooldown--;
			return;
		}

		if (mode == Mode.PUSHING_TO_BELT)
			activateExtractingBeltFunnel();
		if (mode == Mode.HOPPER)
			activateHopper();
	}

	private void activateHopper() {
		if (!invManipulation.hasInventory())
			return;
		int amountToExtract = autoExtractor.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;

		Predicate<ItemStack> filter = s -> !filtering.isActive() || filtering.test(s);
		Function<ItemStack, Integer> amountThreshold = s -> s.getMaxStackSize() - invManipulation.simulate()
			.insert(s)
			.getCount();

		if (amountToExtract != -1 && !invManipulation.simulate()
			.insert(autoExtractor.simulate()
				.extract(amountToExtract, filter))
			.isEmpty())
			return;

		ItemStack stack = autoExtractor.extract(amountToExtract, filter, amountThreshold);
		if (stack.isEmpty())
			return;
		invManipulation.insert(stack);
		startCooldown();
	}

	private void tickAsPullingBeltFunnel() {
		// Belts handle insertion from their side
		if (AllBlocks.BELT.has(world.getBlockState(pos.down())))
			return;
		TransportedItemStackHandlerBehaviour handler =
			TileEntityBehaviour.get(world, pos.down(), TransportedItemStackHandlerBehaviour.TYPE);
		if (handler == null)
			return;
		handler.handleCenteredProcessingOnAllItems(1 / 32f, this::collectFromHandler);
	}

	private void activateExtractingBeltFunnel() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.get(BeltFunnelBlock.HORIZONTAL_FACING);
		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);

		if (inputBehaviour == null)
			return;
		if (!inputBehaviour.canInsertFromSide(facing))
			return;

		int amountToExtract = invManipulation.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;
		ItemStack stack = invManipulation.extract(amountToExtract, s -> inputBehaviour.handleInsertion(s, facing, true)
			.isEmpty());
		if (stack.isEmpty())
			return;
		flap(false);
		inputBehaviour.handleInsertion(stack, facing, false);
		startCooldown();
	}

	private int startCooldown() {
		return extractionCooldown = AllConfigs.SERVER.logistics.defaultExtractionTimer.get();
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
		autoExtractor = InvManipulationBehaviour.forExtraction(this, InterfaceProvider.towardBlockFacing());
		behaviours.add(autoExtractor);

		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());
		filtering.showCountWhen(this::supportsAmountOnFilter);
		filtering.onlyActiveWhen(this::supportsFiltering);
		behaviours.add(filtering);

		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
			.setInsertionHandler(this::handleDirectBeltInput));
	}

	private boolean supportsAmountOnFilter() {
		BlockState blockState = getBlockState();
		boolean pushingToBelt = blockState.getBlock() instanceof HorizontalInteractionFunnelBlock
			&& blockState.get(HorizontalInteractionFunnelBlock.PUSHING);
		boolean hopper = FunnelBlock.getFunnelFacing(blockState) == Direction.UP && invManipulation.hasInventory()
			&& autoExtractor.hasInventory();
		return pushingToBelt || hopper;
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		if (blockState == null)
			return false;
		if (!(blockState.getBlock() instanceof FunnelBlock))
			return false;
		Direction direction = blockState.get(FunnelBlock.FACING);
		return direction == Direction.UP || direction == side.getOpposite();
	}

	private boolean supportsFiltering() {
		BlockState blockState = getBlockState();
		return AllBlocks.BRASS_BELT_FUNNEL.has(blockState) || AllBlocks.BRASS_FUNNEL.has(blockState);
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
		compound.putInt("TransferCooldown", extractionCooldown);
		if (clientPacket && sendFlap != 0) {
			compound.putInt("Flap", sendFlap);
			sendFlap = 0;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		extractionCooldown = compound.getInt("TransferCooldown");
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
