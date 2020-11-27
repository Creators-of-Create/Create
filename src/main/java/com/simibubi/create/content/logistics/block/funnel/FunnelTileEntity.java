package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelTileEntity extends SmartTileEntity implements IHaveHoveringInformation {

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
		if (state.has(BlockStateProperties.POWERED) && state.get(BlockStateProperties.POWERED))
			return Mode.PAUSED;
		if (FunnelBlock.getFunnelFacing(state) == Direction.UP && autoExtractor.hasInventory())
			return Mode.HOPPER;
		if (state.getBlock() instanceof BeltFunnelBlock) {
			Shape shape = state.get(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PULLING)
				return Mode.TAKING_FROM_BELT;
			if (shape == Shape.PUSHING)
				return Mode.PUSHING_TO_BELT;

			BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos.down());
			if (belt != null)
				return belt.getMovementFacing() == state.get(BeltFunnelBlock.HORIZONTAL_FACING) ? Mode.PUSHING_TO_BELT
					: Mode.TAKING_FROM_BELT;
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
			return;

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
		Function<ItemStack, Integer> amountThreshold = s -> {
			int maxStackSize = s.getMaxStackSize();
			return maxStackSize - invManipulation.simulate()
				.insert(ItemHandlerHelper.copyStackWithSize(s, maxStackSize))
				.getCount();
		};

		if (amountToExtract != -1 && !invManipulation.simulate()
			.insert(autoExtractor.simulate()
				.extract(amountToExtract, filter))
			.isEmpty())
			return;

		ItemStack stack = autoExtractor.extract(amountToExtract, filter, amountThreshold);
		if (stack.isEmpty())
			return;

		onTransfer(stack);
		invManipulation.insert(stack);
		startCooldown();
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

		int amountToExtract = getAmountToExtract();
		ItemStack stack = invManipulation.extract(amountToExtract, s -> inputBehaviour.handleInsertion(s, facing, true)
			.isEmpty());
		if (stack.isEmpty())
			return;
		flap(false);
		onTransfer(stack);
		inputBehaviour.handleInsertion(stack, facing, false);
		startCooldown();
	}

	public int getAmountToExtract() {
		if (!supportsAmountOnFilter())
			return -1;
		int amountToExtract = invManipulation.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;
		return amountToExtract;
	}

	private int startCooldown() {
		return extractionCooldown = AllConfigs.SERVER.logistics.defaultExtractionTimer.get();
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
		boolean beltFunnelsupportsAmount = false;
		if (blockState.getBlock() instanceof BeltFunnelBlock) {
			Shape shape = blockState.get(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PUSHING)
				beltFunnelsupportsAmount = true;
			else
				beltFunnelsupportsAmount = BeltHelper.getSegmentTE(world, pos.down()) != null;
		}
		boolean hopper = FunnelBlock.getFunnelFacing(blockState) == Direction.UP && !world.getBlockState(pos.up())
			.getMaterial()
			.isReplaceable();
		return beltFunnelsupportsAmount || hopper;
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
		if (!simulate)
			onTransfer(inserted);
		return invManipulation.insert(inserted);
	}

	public void flap(boolean inward) {
		sendFlap = inward ? 1 : -1;
		sendData();
	}

	public boolean hasFlap() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BeltFunnelBlock))
			return false;
		return true;
	}

	public float getFlapOffset() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BeltFunnelBlock))
			return 0;
		switch (blockState.get(BeltFunnelBlock.SHAPE)) {
		default:
		case RETRACTED:
			return 0;
		case EXTENDED:
			return 8 / 16f;
		case PULLING:
		case PUSHING:
			return -2 / 16f;
		}
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

	public void onTransfer(ItemStack stack) {
		AllBlocks.CONTENT_OBSERVER.get()
			.onFunnelTransfer(world, pos, stack);
	}

	@Override
	// Hint players not to use funnels like 0.2 transposers
	public boolean addToTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		if (isPlayerSneaking)
			return false;
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof FunnelBlock))
			return false;
		Direction funnelFacing = FunnelBlock.getFunnelFacing(state);

		if (world.getBlockState(pos.offset(funnelFacing.getOpposite()))
			.getMaterial()
			.isReplaceable())
			return false;

		BlockPos inputPos = pos.offset(funnelFacing);
		TileEntity tileEntity = world.getTileEntity(inputPos);
		if (tileEntity == null)
			return false;
		if (tileEntity instanceof BeltTileEntity)
			return false;
		if (tileEntity instanceof SawTileEntity)
			return false;
		if (tileEntity instanceof ChuteTileEntity)
			return false;

		LazyOptional<IItemHandler> capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!capability.isPresent())
			return false;

		if (funnelFacing == Direction.DOWN) {
			TooltipHelper.addHint(tooltip, "hint.upward_funnel");
			return true;
		}
		if (!funnelFacing.getAxis()
			.isHorizontal())
			return false;

		TooltipHelper.addHint(tooltip, "hint.horizontal_funnel");
		return true;
	}

}
