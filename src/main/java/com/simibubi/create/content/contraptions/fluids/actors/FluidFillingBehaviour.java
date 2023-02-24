package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BBHelper;
import com.simibubi.create.foundation.utility.Iterate;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
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

public class FluidFillingBehaviour extends FluidManipulationBehaviour {

	public static final BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();

	PriorityQueue<BlockPosEntry> queue;

	List<BlockPosEntry> infinityCheckFrontier;
	Set<BlockPos> infinityCheckVisited;

	public FluidFillingBehaviour(SmartBlockEntity be) {
		super(be);
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
		try {
			search(fluid, infinityCheckFrontier, infinityCheckVisited,
				(p, d) -> infinityCheckFrontier.add(new BlockPosEntry(p, d)), true);
		} catch (ChunkNotLoadedException e) {
			infinityCheckFrontier.clear();
			infinityCheckVisited.clear();
			setLongValidationTimer();
			return;
		}
		
		int maxBlocks = maxBlocks();

		if (infinityCheckVisited.size() > maxBlocks && maxBlocks != -1 && !fillInfinite()) {
			if (!infinite) {
				reset();
				infinite = true;
				blockEntity.sendData();
			}
			infinityCheckFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!infinityCheckFrontier.isEmpty())
			return;
		if (infinite) {
			reset();
			return;
		}

		infinityCheckVisited.clear();
	}

	public boolean tryDeposit(Fluid fluid, BlockPos root, boolean simulate) {
		if (!Objects.equals(root, rootPos)) {
			reset();
			rootPos = root;
			queue.enqueue(new BlockPosEntry(root, 0));
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
			.ultraWarm() && FluidHelper.isTag(fluid, FluidTags.WATER);
		boolean canPlaceSources = AllConfigs.server().fluids.placeFluidSourceBlocks.get();

		if ((!fillInfinite() && infinite) || evaporate || !canPlaceSources) {
			FluidState fluidState = world.getFluidState(rootPos);
			boolean equivalentTo = fluidState.getType()
				.isSame(fluid);
			if (!equivalentTo && !evaporate && canPlaceSources)
				return false;
			if (simulate)
				return true;
			playEffect(world, root, fluid, false);
			if (evaporate) {
				int i = root.getX();
				int j = root.getY();
				int k = root.getZ();
				world.playSound(null, i, j, k, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
					2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			} else if (!canPlaceSources)
				blockEntity.award(AllAdvancements.HOSE_PULLEY);
			return true;
		}

		boolean success = false;
		for (int i = 0; !success && !queue.isEmpty() && i < searchedPerTick; i++) {
			BlockPosEntry entry = queue.first();
			BlockPos currentPos = entry.pos();

			if (visited.contains(currentPos)) {
				queue.dequeue();
				continue;
			}

			if (!simulate)
				visited.add(currentPos);

			if (visited.size() >= maxBlocks && maxBlocks != -1) {
				infinite = true;
				if (!fillInfinite()) {
					visited.clear();
					queue.clear();
					return false;
				}
			}

			SpaceType spaceType = getAtPos(world, currentPos, fluid);
			if (spaceType == SpaceType.BLOCKING)
				continue;
			if (spaceType == SpaceType.FILLABLE) {
				success = true;
				if (!simulate) {
					playEffect(world, currentPos, fluid, false);

					BlockState blockState = world.getBlockState(currentPos);
					if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && fluid.isSame(Fluids.WATER)) {
						if (!blockEntity.isVirtual())
							world.setBlock(currentPos,
								updatePostWaterlogging(blockState.setValue(BlockStateProperties.WATERLOGGED, true)),
								2 | 16);
					} else {
						replaceBlock(world, currentPos, blockState);
						if (!blockEntity.isVirtual())
							world.setBlock(currentPos, FluidHelper.convertToStill(fluid)
								.defaultFluidState()
								.createLegacyBlock(), 2 | 16);
					}

					LevelTickAccess<Fluid> pendingFluidTicks = world.getFluidTicks();
					if (pendingFluidTicks instanceof LevelTicks) {
						LevelTicks<Fluid> serverTickList = (LevelTicks<Fluid>) pendingFluidTicks;
						serverTickList.clearArea(new BoundingBox(currentPos));
					}

					affectedArea = BBHelper.encapsulate(affectedArea, currentPos);
				}
			}

			if (simulate && success)
				return true;

			visited.add(currentPos);
			queue.dequeue();

			for (Direction side : Iterate.directions) {
				if (side == Direction.UP)
					continue;

				BlockPos offsetPos = currentPos.relative(side);
				if (visited.contains(offsetPos))
					continue;
				if (offsetPos.distSqr(rootPos) > maxRangeSq)
					continue;

				SpaceType nextSpaceType = getAtPos(world, offsetPos, fluid);
				if (nextSpaceType != SpaceType.BLOCKING)
					queue.enqueue(new BlockPosEntry(offsetPos, entry.distance() + 1));
			}
		}

		if (!simulate && success)
			blockEntity.award(AllAdvancements.HOSE_PULLEY);
		return success;
	}

	protected void softReset(BlockPos root) {
		visited.clear();
		queue.clear();
		queue.enqueue(new BlockPosEntry(root, 0));
		infinite = false;
		setValidationTimer();
		blockEntity.sendData();
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

	protected void replaceBlock(Level world, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropResources(state, world, pos, blockEntity);
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
	public void reset() {
		super.reset();
		queue.clear();
		infinityCheckFrontier.clear();
		infinityCheckVisited.clear();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
