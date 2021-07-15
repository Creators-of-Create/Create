package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.simibubi.create.content.contraptions.fluids.actors.FluidManipulationBehaviour.BlockPosEntry;

public class FluidDrainingBehaviour extends FluidManipulationBehaviour {

	Fluid fluid;

	// Execution
	Set<BlockPos> validationSet;
	PriorityQueue<BlockPosEntry> queue;
	boolean isValid;

	// Validation
	List<BlockPosEntry> validationFrontier;
	Set<BlockPos> validationVisited;
	Set<BlockPos> newValidationSet;

	public FluidDrainingBehaviour(SmartTileEntity te) {
		super(te);
		validationVisited = new HashSet<>();
		validationFrontier = new ArrayList<>();
		validationSet = new HashSet<>();
		newValidationSet = new HashSet<>();
		queue = new ObjectHeapPriorityQueue<>(this::comparePositions);
	}

	@Nullable
	public boolean pullNext(BlockPos root, boolean simulate) {
		if (!frontier.isEmpty())
			return false;
		if (!Objects.equals(root, rootPos)) {
			rebuildContext(root);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root);
			return false;
		}

		if (affectedArea == null)
			affectedArea = new MutableBoundingBox(root, root);

		World world = getWorld();
		if (!queue.isEmpty() && !isValid) {
			rebuildContext(root);
			return false;
		}

		if (validationFrontier.isEmpty() && !queue.isEmpty() && !simulate && revalidateIn == 0)
			revalidate(root);

		while (!queue.isEmpty()) {
			// Dont dequeue here, so we can decide not to dequeue a valid entry when
			// simulating
			BlockPos currentPos = queue.first().pos;
			BlockState blockState = world.getBlockState(currentPos);
			BlockState emptied = blockState;
			Fluid fluid = Fluids.EMPTY;

			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
				emptied = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
				fluid = Fluids.WATER;
			} else if (blockState.getBlock() instanceof FlowingFluidBlock) {
				FlowingFluidBlock flowingFluid = (FlowingFluidBlock) blockState.getBlock();
				emptied = Blocks.AIR.defaultBlockState();
				if (blockState.getValue(FlowingFluidBlock.LEVEL) == 0)
					fluid = flowingFluid.getFluid();
				else {
					affectedArea.expand(new MutableBoundingBox(currentPos, currentPos));
					if (!tileEntity.isVirtual())
						world.setBlock(currentPos, emptied, 2 | 16);
					queue.dequeue();
					if (queue.isEmpty()) {
						isValid = checkValid(world, rootPos);
						reset();
					}
					continue;
				}
			} else if (blockState.getFluidState()
				.getType() != Fluids.EMPTY
				&& blockState.getCollisionShape(world, currentPos, ISelectionContext.empty())
					.isEmpty()) {
				fluid = blockState.getFluidState()
					.getType();
				emptied = Blocks.AIR.defaultBlockState();
			}

			if (this.fluid == null)
				this.fluid = fluid;

			if (!this.fluid.isSame(fluid)) {
				queue.dequeue();
				if (queue.isEmpty()) {
					isValid = checkValid(world, rootPos);
					reset();
				}
				continue;
			}

			if (simulate)
				return true;

			playEffect(world, currentPos, fluid, true);
			AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.getBlockPos(), 8);

			if (infinite) {
				AllTriggers.triggerForNearbyPlayers(AllTriggers.INFINITE_FLUID.constructTriggerFor(FluidHelper.convertToStill(fluid)), world, tileEntity.getBlockPos(), 8);
				return true;
			}

			if (!tileEntity.isVirtual())
				world.setBlock(currentPos, emptied, 2 | 16);
			affectedArea.expand(new MutableBoundingBox(currentPos, currentPos));

			queue.dequeue();
			if (queue.isEmpty()) {
				isValid = checkValid(world, rootPos);
				reset();
			} else if (!validationSet.contains(currentPos)) {
				reset();
			}
			return true;
		}

		if (rootPos == null)
			return false;

		if (isValid)
			rebuildContext(root);

		return false;
	}

	protected void softReset(BlockPos root) {
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		visited.clear();
		infinite = false;
		setValidationTimer();
		frontier.add(new BlockPosEntry(root, 0));
		tileEntity.sendData();
	}

	protected boolean checkValid(World world, BlockPos root) {
		BlockPos currentPos = root;
		for (int timeout = 1000; timeout > 0 && !root.equals(tileEntity.getBlockPos()); timeout--) {
			FluidBlockType canPullFluidsFrom = canPullFluidsFrom(world.getBlockState(currentPos), currentPos);
			if (canPullFluidsFrom == FluidBlockType.FLOWING) {
				currentPos = currentPos.above();
				continue;
			}
			if (canPullFluidsFrom == FluidBlockType.SOURCE)
				return true;
			break;
		}
		return false;
	}

	enum FluidBlockType {
		NONE, SOURCE, FLOWING;
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		if (!clientPacket && affectedArea != null)
			frontier.add(new BlockPosEntry(rootPos, 0));
	}

	protected FluidBlockType canPullFluidsFrom(BlockState blockState, BlockPos pos) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED))
			return FluidBlockType.SOURCE;
		if (blockState.getBlock() instanceof FlowingFluidBlock)
			return blockState.getValue(FlowingFluidBlock.LEVEL) == 0 ? FluidBlockType.SOURCE : FluidBlockType.FLOWING;
		if (blockState.getFluidState()
			.getType() != Fluids.EMPTY && blockState.getCollisionShape(getWorld(), pos, ISelectionContext.empty())
				.isEmpty())
			return FluidBlockType.SOURCE;
		return FluidBlockType.NONE;
	}

	@Override
	public void tick() {
		super.tick();
		if (rootPos != null)
			isValid = checkValid(getWorld(), rootPos);
		if (!frontier.isEmpty()) {
			continueSearch();
			return;
		}
		if (!validationFrontier.isEmpty()) {
			continueValidation();
			return;
		}
		if (revalidateIn > 0)
			revalidateIn--;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
	}

	public void rebuildContext(BlockPos root) {
		reset();
		rootPos = root;
		affectedArea = new MutableBoundingBox(rootPos, rootPos);
		if (isValid)
			frontier.add(new BlockPosEntry(root, 0));
	}

	public void revalidate(BlockPos root) {
		validationFrontier.clear();
		validationVisited.clear();
		newValidationSet.clear();
		validationFrontier.add(new BlockPosEntry(root, 0));
		setValidationTimer();
	}

	private void continueSearch() {
		fluid = search(fluid, frontier, visited, (e, d) -> {
			queue.enqueue(new BlockPosEntry(e, d));
			validationSet.add(e);
		}, false);

		World world = getWorld();
		int maxBlocks = maxBlocks();
		if (visited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			infinite = true;
			// Find first block with valid fluid
			while (true) {
				BlockPos first = queue.first().pos;
				if (canPullFluidsFrom(world.getBlockState(first), first) != FluidBlockType.SOURCE) {
					queue.dequeue();
					continue;
				}
				break;
			}
			BlockPos firstValid = queue.first().pos;
			frontier.clear();
			visited.clear();
			queue.clear();
			queue.enqueue(new BlockPosEntry(firstValid, 0));
			tileEntity.sendData();
			return;
		}

		if (!frontier.isEmpty())
			return;

		tileEntity.sendData();
		visited.clear();
	}

	private void continueValidation() {
		search(fluid, validationFrontier, validationVisited, (e, d) -> newValidationSet.add(e), false);

		int maxBlocks = maxBlocks();
		if (validationVisited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			if (!infinite)
				reset();
			validationFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!validationFrontier.isEmpty())
			return;
		if (infinite) {
			reset();
			return;
		}

		validationSet = newValidationSet;
		newValidationSet = new HashSet<>();
		validationVisited.clear();
	}

	@Override
	public void reset() {
		super.reset();

		fluid = null;
		rootPos = null;
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		tileEntity.sendData();
	}

	public static BehaviourType<FluidDrainingBehaviour> TYPE = new BehaviourType<>();

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	protected boolean isSearching() {
		return !frontier.isEmpty();
	}

	public FluidStack getDrainableFluid(BlockPos rootPos) {
		return fluid == null || isSearching() || !pullNext(rootPos, true) ? FluidStack.EMPTY
			: new FluidStack(fluid, 1000);
	}

}
