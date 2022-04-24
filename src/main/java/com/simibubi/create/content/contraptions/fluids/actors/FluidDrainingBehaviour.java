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

import io.github.fabricators_of_create.porting_lib.extensions.LevelExtensions;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.LiquidBlockAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FluidDrainingBehaviour extends FluidManipulationBehaviour {

	Fluid fluid;

	// Execution
	Set<BlockPos> validationSet;
	PriorityQueue<BlockPosEntry> queue;
	List<BlockPosEntry> queueList = new ObjectArrayList<>();
	boolean isValid;

	// Validation
	List<BlockPosEntry> validationFrontier;
	Set<BlockPos> validationVisited;
	Set<BlockPos> newValidationSet;

	SnapshotParticipant<Data> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected Data createSnapshot() {
			FluidDrainingBehaviour b = FluidDrainingBehaviour.this;
			BlockPos rootPos = b.rootPos == null ? null : b.rootPos.immutable();
			BoundingBox box = b.affectedArea == null ? null : new BoundingBox(
					affectedArea.minX(), affectedArea.minY(), affectedArea.minZ(),
					affectedArea.maxX(), affectedArea.maxY(), affectedArea.maxZ()
			);

			return new Data(
					rootPos, new ArrayList<>(validationFrontier), new HashSet<>(validationVisited),
					new HashSet<>(newValidationSet), revalidateIn, box, new ObjectArrayList<>(queueList)
			);
		}

		@Override
		protected void readSnapshot(Data snapshot) {
			validationFrontier = snapshot.validationFrontier;
			validationVisited = snapshot.validationVisited;
			newValidationSet = snapshot.newValidationSet;
			revalidateIn = snapshot.revalidateIn;
			affectedArea = snapshot.affectedArea;
			queueList = snapshot.queueList;
			rootPos = snapshot.rootPos;
			queue = new ObjectHeapPriorityQueue<>(queueList, FluidDrainingBehaviour.this::comparePositions);
		}
	};

	@Override
	protected SnapshotParticipant<?> snapshotParticipant() {
		return snapshotParticipant;
	}

	record Data(BlockPos rootPos, List<BlockPosEntry> validationFrontier,
				Set<BlockPos> validationVisited, Set<BlockPos> newValidationSet,
				int revalidateIn, BoundingBox affectedArea,
				ObjectArrayList<BlockPosEntry> queueList) {
	}

	public FluidDrainingBehaviour(SmartTileEntity te) {
		super(te);
		validationVisited = new HashSet<>();
		validationFrontier = new ArrayList<>();
		validationSet = new HashSet<>();
		newValidationSet = new HashSet<>();
		queue = new ObjectHeapPriorityQueue<>(this::comparePositions);
	}

	@Nullable
	public boolean pullNext(BlockPos root, TransactionContext ctx) {
		if (!frontier.isEmpty())
			return false;
		if (!Objects.equals(root, rootPos)) {
			rebuildContext(root, ctx);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root, ctx);
			return false;
		}

		if (affectedArea == null)
			affectedArea = BoundingBox.fromCorners(root, root);

		Level world = getWorld();
		if (!queue.isEmpty() && !isValid) {
			rebuildContext(root, ctx);
			return false;
		}

		snapshotParticipant.updateSnapshots(ctx);
		if (validationFrontier.isEmpty() && !queue.isEmpty() && revalidateIn == 0)
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
			} else if (blockState.getBlock() instanceof LiquidBlock) {
				LiquidBlock flowingFluid = (LiquidBlock) blockState.getBlock();
				emptied = Blocks.AIR.defaultBlockState();
				if (blockState.getValue(LiquidBlock.LEVEL) == 0)
					fluid = ((LiquidBlockAccessor) flowingFluid).port_lib$getFluid().getSource();
				else {
					affectedArea.encapsulate(BoundingBox.fromCorners(currentPos, currentPos));
					if (!tileEntity.isVirtual())
						world.setBlock(currentPos, emptied, 2 | 16);
					BlockPosEntry e = queue.dequeue();
					queueList.remove(e);
					if (queue.isEmpty()) {
						isValid = checkValid(world, rootPos);
						reset(ctx);
					}
					continue;
				}
			} else if (blockState.getFluidState()
				.getType() != Fluids.EMPTY
				&& blockState.getCollisionShape(world, currentPos, CollisionContext.empty())
					.isEmpty()) {
				fluid = blockState.getFluidState()
					.getType();
				emptied = Blocks.AIR.defaultBlockState();
			}

			if (this.fluid == null)
				this.fluid = fluid;

			if (!this.fluid.isSame(fluid)) {
				BlockPosEntry e = queue.dequeue();
				queueList.remove(e);
				if (queue.isEmpty()) {
					isValid = checkValid(world, rootPos);
					reset(ctx);
				}
				continue;
			}

			Fluid finalFluid = fluid;
			TransactionCallback.onSuccess(ctx, () -> {
				playEffect(world, currentPos, finalFluid, true);
				AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.getBlockPos(), 8);

				if (infinite) {
					AllTriggers.triggerForNearbyPlayers(AllTriggers.INFINITE_FLUID.constructTriggerFor(FluidHelper.convertToStill(finalFluid)), world, tileEntity.getBlockPos(), 8);
				}
			});

			if (infinite) {
				return true;
			}

			if (!tileEntity.isVirtual()) {
				((LevelExtensions) world).updateSnapshots(ctx);
				world.setBlock(currentPos, emptied, 2 | 16);
			}
			affectedArea.encapsulate(BoundingBox.fromCorners(currentPos, currentPos));

			BlockPosEntry e = queue.dequeue();
			queueList.remove(e);
			if (queue.isEmpty()) {
				isValid = checkValid(world, rootPos);
				reset(ctx);
			} else if (!validationSet.contains(currentPos)) {
				reset(ctx);
			}
			return true;
		}

		if (rootPos == null)
			return false;

		if (isValid)
			rebuildContext(root, ctx);

		return false;
	}

	protected void softReset(BlockPos root, TransactionContext ctx) {
		queue.clear();
		queueList.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		visited.clear();
		infinite = false;
		setValidationTimer();
		frontier.add(new BlockPosEntry(root, 0));
		TransactionCallback.onSuccess(ctx, tileEntity::sendData);
	}

	protected boolean checkValid(Level world, BlockPos root) {
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
	public void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		if (!clientPacket && affectedArea != null)
			frontier.add(new BlockPosEntry(rootPos, 0));
	}

	protected FluidBlockType canPullFluidsFrom(BlockState blockState, BlockPos pos) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED))
			return FluidBlockType.SOURCE;
		if (blockState.getBlock() instanceof LiquidBlock)
			return blockState.getValue(LiquidBlock.LEVEL) == 0 ? FluidBlockType.SOURCE : FluidBlockType.FLOWING;
		if (blockState.getFluidState()
			.getType() != Fluids.EMPTY && blockState.getCollisionShape(getWorld(), pos, CollisionContext.empty())
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

	public void rebuildContext(BlockPos root, TransactionContext ctx) {
		reset(ctx);
		rootPos = root;
		affectedArea = BoundingBox.fromCorners(rootPos, rootPos);
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
			BlockPosEntry entry = new BlockPosEntry(e, d);
			queue.enqueue(entry);
			queueList.add(entry);
			validationSet.add(e);
		}, false);

		Level world = getWorld();
		int maxBlocks = maxBlocks();
		if (visited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			infinite = true;
			// Find first block with valid fluid
			while (true) {
				BlockPos first = queue.first().pos;
				if (canPullFluidsFrom(world.getBlockState(first), first) != FluidBlockType.SOURCE) {
					BlockPosEntry e = queue.dequeue();
					queueList.remove(e);
					continue;
				}
				break;
			}
			BlockPos firstValid = queue.first().pos;
			frontier.clear();
			visited.clear();
			queue.clear();
			queueList.clear();
			BlockPosEntry e = new BlockPosEntry(firstValid, 0);
			queue.enqueue(e);
			queueList.add(e);
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
				reset(null);
			validationFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!validationFrontier.isEmpty())
			return;
		if (infinite) {
			reset(null);
			return;
		}

		validationSet = newValidationSet;
		newValidationSet = new HashSet<>();
		validationVisited.clear();
	}

	@Override
	public void reset(@Nullable TransactionContext ctx) {
		super.reset(ctx);

		fluid = null;
		rootPos = null;
		queue.clear();
		queueList.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		if (ctx != null) TransactionCallback.onSuccess(ctx, tileEntity::sendData);
		else tileEntity.sendData();
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
		try (Transaction t = TransferUtil.getTransaction()) {
			if (fluid == null || isSearching() || !pullNext(rootPos, t)) {
				return FluidStack.EMPTY;
			} else if (fluid == null) { // fabric: we need to check again because null isn't allowed and search/pull can set to null
				return FluidStack.EMPTY;
			} else {
				return new FluidStack(fluid, FluidConstants.BUCKET);
			}
		}
	}

}
