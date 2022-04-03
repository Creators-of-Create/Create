package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.Iterate;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionSuccessCallback;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.LevelTicks;

import org.lwjgl.system.CallbackI.B;

import javax.annotation.Nullable;

public class FluidFillingBehaviour extends FluidManipulationBehaviour {

	PriorityQueue<BlockPosEntry> queue;
	List<BlockPosEntry> queueList = new ObjectArrayList<>();

	List<BlockPosEntry> infinityCheckFrontier;
	Set<BlockPos> infinityCheckVisited;

	SnapshotParticipant<Data> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected Data createSnapshot() {
			return new Data(new HashSet<>(visited), new ObjectArrayList<>(queueList), counterpartActed);
		}

		@Override
		protected void readSnapshot(Data snapshot) {
			visited = snapshot.visited;
			queueList = snapshot.queueList;
			queue = new ObjectHeapPriorityQueue<>(queueList, (p, p2) -> -comparePositions(p, p2));
			counterpartActed = snapshot.counterpartActed;
		}
	};

	@Override
	protected SnapshotParticipant<?> snapshotParticipant() {
		return snapshotParticipant;
	}

	record Data(Set<BlockPos> visited, List<BlockPosEntry> queueList, boolean counterpartActed) {
	}

	public FluidFillingBehaviour(SmartTileEntity te) {
		super(te);
		queue = new ObjectHeapPriorityQueue<>((p, p2) -> -comparePositions(p, p2));
		revalidateIn = 1;
		infinityCheckFrontier = new ArrayList<>();
		infinityCheckVisited = new HashSet<>();
	}

	@Override
	public void tick() {
		super.tick();
		if (!infinityCheckFrontier.isEmpty() && rootPos != null) {
			Fluid fluid = getWorld().getFluidState(rootPos)
				.getType();
			if (fluid != Fluids.EMPTY)
				continueValidation(fluid);
		}
		if (revalidateIn > 0)
			revalidateIn--;
	}

	protected void continueValidation(Fluid fluid) {
		search(fluid, infinityCheckFrontier, infinityCheckVisited,
			(p, d) -> infinityCheckFrontier.add(new BlockPosEntry(p, d)), true);
		int maxBlocks = maxBlocks();

		if (infinityCheckVisited.size() > maxBlocks && maxBlocks != -1) {
			if (!infinite) {
				reset(null);
				infinite = true;
				tileEntity.sendData();
			}
			infinityCheckFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!infinityCheckFrontier.isEmpty())
			return;
		if (infinite) {
			reset(null);
			return;
		}

		infinityCheckVisited.clear();
	}

	public boolean tryDeposit(Fluid fluid, BlockPos root, TransactionContext ctx) {
		if (!Objects.equals(root, rootPos)) {
			reset(ctx);
			rootPos = root;
			BlockPosEntry e = new BlockPosEntry(root, 0);
			queue.enqueue(e);
			queueList.add(e);
			affectedArea = BoundingBox.fromCorners(rootPos, rootPos);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root);
			return false;
		}

		if (affectedArea == null)
			affectedArea = BoundingBox.fromCorners(root, root);

		if (revalidateIn == 0) {
			visited.clear();
			infinityCheckFrontier.clear();
			infinityCheckVisited.clear();
			infinityCheckFrontier.add(new BlockPosEntry(root, 0));
			setValidationTimer();
			softReset(root);
		}

		Level world = getWorld();
		int maxRange = maxRange();
		int maxRangeSq = maxRange * maxRange;
		int maxBlocks = maxBlocks();
		boolean evaporate = world.dimensionType()
			.ultraWarm() && fluid.is(FluidTags.WATER);
		boolean canPlaceSources = AllConfigs.SERVER.fluids.placeFluidSourceBlocks.get();

		if ((!fillInfinite() && infinite) || evaporate || !canPlaceSources) {
			FluidState fluidState = world.getFluidState(rootPos);
			boolean equivalentTo = fluidState.getType()
				.isSame(fluid);
			if (!equivalentTo && !evaporate && canPlaceSources)
				return false;

			TransactionCallback.onSuccess(ctx, () -> {
				playEffect(world, root, fluid, false);
				if (evaporate) {
					int i = root.getX();
					int j = root.getY();
					int k = root.getZ();
					world.playSound(null, i, j, k, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
							2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
				} else if (!canPlaceSources)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.getBlockPos(), 8);
			});
			return true;
		}

		boolean success = false;
		for (int i = 0; !success && !queue.isEmpty() && i < searchedPerTick; i++) {
			BlockPosEntry entry = queue.first();
			BlockPos currentPos = entry.pos;

			if (visited.contains(currentPos)) {
				BlockPosEntry e = queue.dequeue();
				queueList.remove(e);
				continue;
			}

			snapshotParticipant.updateSnapshots(ctx);
			visited.add(currentPos);

			if (visited.size() >= maxBlocks && maxBlocks != -1) {
				infinite = true;
				visited.clear();
				queue.clear();
				queueList.clear();
				return false;
			}

			SpaceType spaceType = getAtPos(world, currentPos, fluid);
			if (spaceType == SpaceType.BLOCKING)
				continue;
			if (spaceType == SpaceType.FILLABLE) {
				success = true;
				BlockState blockState = world.getBlockState(currentPos);
				if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && fluid.isSame(Fluids.WATER)) {
					if (!tileEntity.isVirtual())
						TransactionCallback.setBlock(ctx, world, currentPos,
								updatePostWaterlogging(blockState.setValue(BlockStateProperties.WATERLOGGED, true)),
								2 | 16);
				} else {
					replaceBlock(world, currentPos, blockState, ctx);
					if (!tileEntity.isVirtual())
						TransactionCallback.setBlock(ctx, world, currentPos, FluidHelper.convertToStill(fluid)
								.defaultFluidState()
								.createLegacyBlock(), 2 | 16);
				}

				TransactionCallback.onSuccess(ctx, () -> {
					playEffect(world, currentPos, fluid, false);
					LevelTickAccess<Fluid> pendingFluidTicks = world.getFluidTicks();
					if (pendingFluidTicks instanceof LevelTicks) {
						LevelTicks<Fluid> serverTickList = (LevelTicks<Fluid>) pendingFluidTicks;
						serverTickList.clearArea(new BoundingBox(currentPos));
					}

					affectedArea.encapsulate(BoundingBox.fromCorners(currentPos, currentPos));
				});
			}

			visited.add(currentPos);
			BlockPosEntry e = queue.dequeue();
			queueList.remove(e);

			for (Direction side : Iterate.directions) {
				if (side == Direction.UP)
					continue;

				BlockPos offsetPos = currentPos.relative(side);
				if (visited.contains(offsetPos))
					continue;
				if (offsetPos.distSqr(rootPos) > maxRangeSq)
					continue;

				SpaceType nextSpaceType = getAtPos(world, offsetPos, fluid);
				if (nextSpaceType != SpaceType.BLOCKING) {
					BlockPosEntry posEntry = new BlockPosEntry(offsetPos, entry.distance + 1);
					queue.enqueue(posEntry);
					queueList.add(posEntry);
				}

			}
		}

		if (success)
			TransactionCallback.onSuccess(ctx, () -> AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.getBlockPos(), 8));
		return success;
	}

	protected void softReset(BlockPos root) {
		visited.clear();
		queue.clear();
		queueList.clear();
		BlockPosEntry e = new BlockPosEntry(root, 0);
		queue.enqueue(e);
		queueList.add(e);
		infinite = false;
		setValidationTimer();
		tileEntity.sendData();
	}

	enum SpaceType {
		FILLABLE, FILLED, BLOCKING
	}

	protected SpaceType getAtPos(Level world, BlockPos pos, Fluid toFill) {
		BlockState blockState = world.getBlockState(pos);
		FluidState fluidState = blockState.getFluidState();

		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED))
			return toFill.isSame(Fluids.WATER)
				? blockState.getValue(BlockStateProperties.WATERLOGGED) ? SpaceType.FILLED : SpaceType.FILLABLE
				: SpaceType.BLOCKING;

		if (blockState.getBlock() instanceof LiquidBlock)
			return blockState.getValue(LiquidBlock.LEVEL) == 0
				? toFill.isSame(fluidState.getType()) ? SpaceType.FILLED : SpaceType.BLOCKING
				: SpaceType.FILLABLE;

		if (fluidState.getType() != Fluids.EMPTY
			&& blockState.getCollisionShape(getWorld(), pos, CollisionContext.empty())
				.isEmpty())
			return toFill.isSame(fluidState.getType()) ? SpaceType.FILLED : SpaceType.BLOCKING;

		return canBeReplacedByFluid(world, pos, blockState) ? SpaceType.FILLABLE : SpaceType.BLOCKING;
	}

	protected void replaceBlock(Level world, BlockPos pos, BlockState state, TransactionContext ctx) {
		TransactionCallback.onSuccess(ctx, () -> {
			BlockEntity tileentity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
			Block.dropResources(state, world, pos, tileentity);
		});
	}

	// From FlowingFluidBlock#isBlocked
	protected boolean canBeReplacedByFluid(BlockGetter world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (!(block instanceof DoorBlock) && !state.is(BlockTags.SIGNS) && block != Blocks.LADDER
			&& block != Blocks.SUGAR_CANE && block != Blocks.BUBBLE_COLUMN) {
			Material material = state.getMaterial();
			if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT
				&& material != Material.REPLACEABLE_WATER_PLANT) {
				return !material.blocksMotion();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected BlockState updatePostWaterlogging(BlockState state) {
		if (state.hasProperty(BlockStateProperties.LIT))
			state = state.setValue(BlockStateProperties.LIT, false);
		return state;
	}

	@Override
	public void reset(@Nullable TransactionContext ctx) {
		super.reset(ctx);
		queue.clear();
		queueList.clear();
		infinityCheckFrontier.clear();
		infinityCheckVisited.clear();
	}

	public static BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
