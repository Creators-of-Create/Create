package com.simibubi.create.content.logistics.block.funnel;

import java.lang.ref.WeakReference;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.packet.FunnelFlapPacket;
import com.simibubi.create.content.logistics.packet.TunnelFlapPacket;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class FunnelTileEntity extends SmartTileEntity implements IHaveHoveringInformation, IInstanceRendered {

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour invManipulation;
	private int extractionCooldown;

	private WeakReference<ItemEntity> lastObserved; // In-world Extractors only

	InterpolatedChasingValue flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, PUSHING_TO_BELT, TAKING_FROM_BELT, EXTRACT
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
			return Mode.INVALID;
		}
		if (state.getBlock() instanceof FunnelBlock)
			return state.get(FunnelBlock.EXTRACTING) ? Mode.EXTRACT : Mode.COLLECT;

		return Mode.INVALID;
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
		if (mode == Mode.EXTRACT)
			activateExtractor();
	}

	private void activateExtractor() {
		BlockState blockState = getBlockState();
		Direction facing = AbstractFunnelBlock.getFunnelFacing(blockState);

		if (facing == null)
			return;

		boolean trackingEntityPresent = true;
		AxisAlignedBB area = getEntityOverflowScanningArea();

		// Check if last item is still blocking the extractor
		if (lastObserved == null) {
			trackingEntityPresent = false;
		} else {
			ItemEntity lastEntity = lastObserved.get();
			if (lastEntity == null || !lastEntity.isAlive() || !lastEntity.getBoundingBox()
				.intersects(area)) {
				trackingEntityPresent = false;
				lastObserved = null;
			}
		}

		if (trackingEntityPresent)
			return;

		// Find other entities blocking the extract (only if necessary)
		int amountToExtract = getAmountToExtract();
		ItemStack stack = invManipulation.simulate()
			.extract(amountToExtract);
		if (stack.isEmpty())
			return;
		for (ItemEntity itemEntity : world.getEntitiesWithinAABB(ItemEntity.class, area)) {
			lastObserved = new WeakReference<>(itemEntity);
			return;
		}

		// Extract
		stack = invManipulation.extract(amountToExtract);
		if (stack.isEmpty())
			return;

		flap(false);
		onTransfer(stack);

		Vec3d outputPos = VecHelper.getCenterOf(pos);
		boolean vertical = facing.getAxis()
			.isVertical();
		boolean up = facing == Direction.UP;

		outputPos = outputPos.add(new Vec3d(facing.getDirectionVec()).scale(vertical ? up ? .15f : .5f : .25f));
		if (!vertical)
			outputPos = outputPos.subtract(0, .45f, 0);

		Vec3d motion = Vec3d.ZERO;
		if (up)
			motion = new Vec3d(0, 4 / 16f, 0);

		ItemEntity item = new ItemEntity(world, outputPos.x, outputPos.y, outputPos.z, stack.copy());
		item.setDefaultPickupDelay();
		item.setMotion(motion);
		world.addEntity(item);
		lastObserved = new WeakReference<>(item);

		startCooldown();
	}

	static final AxisAlignedBB coreBB =
		new AxisAlignedBB(VecHelper.CENTER_OF_ORIGIN, VecHelper.CENTER_OF_ORIGIN).grow(.75f);

	private AxisAlignedBB getEntityOverflowScanningArea() {
		Direction facing = AbstractFunnelBlock.getFunnelFacing(getBlockState());
		AxisAlignedBB bb = coreBB.offset(pos);
		if (facing == null || facing == Direction.UP)
			return bb;
		return bb.expand(0, -1, 0);
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
		invManipulation =
			new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, AbstractFunnelBlock.getFunnelFacing(s)
				.getOpposite()));
		behaviours.add(invManipulation);

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
		boolean extractor = blockState.getBlock() instanceof FunnelBlock && blockState.get(FunnelBlock.EXTRACTING);
		return beltFunnelsupportsAmount || extractor;
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		if (blockState == null)
			return false;
		if (!(blockState.getBlock() instanceof FunnelBlock))
			return false;
		return FunnelBlock.getFunnelFacing(blockState) == Direction.UP;
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
		if (!world.isRemote) {
			AllPackets.channel.send(packetTarget(), new FunnelFlapPacket(this, inward));
		} else {
			flap.set(inward ? 1 : -1);
		}
	}

	public boolean hasFlap() {
		BlockState blockState = getBlockState();
		if (!AbstractFunnelBlock.getFunnelFacing(blockState)
			.getAxis()
			.isHorizontal())
			return false;
		return true;
	}

	public float getFlapOffset() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof BeltFunnelBlock))
			return -1 / 16f;
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
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		extractionCooldown = compound.getInt("TransferCooldown");

		if (clientPacket)
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FastRenderDispatcher.enqueueUpdate(this));
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
