package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.AllTags;

import com.simibubi.create.compat.Mods;

import com.simibubi.create.compat.dynamictrees.DynamicTree;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.KelpTopBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TreeCutter {
	public static final Tree NO_TREE = new Tree(Collections.emptyList(), Collections.emptyList());

	public static boolean canDynamicTreeCutFrom(Block startBlock) {
		return Mods.DYNAMICTREES.runIfInstalled(() -> () -> DynamicTree.isDynamicBranch(startBlock)).orElse(false);
	}

	@Nonnull
	public static Optional<AbstractBlockBreakQueue> findDynamicTree(Block startBlock, BlockPos pos) {
		if (canDynamicTreeCutFrom(startBlock)) {
			return Mods.DYNAMICTREES.runIfInstalled(() -> () -> new DynamicTree(pos));
		}
		return Optional.empty();
	}

	/**
	 * Finds a tree at the given pos. Block at the position should be air
	 *
	 * @param world
	 * @param pos
	 * @return null if not found or not fully cut
	 */
	@Nonnull
	public static AbstractBlockBreakQueue findTree(@Nullable IBlockReader world, BlockPos pos) {
		if (world == null)
			return NO_TREE;

		List<BlockPos> logs = new ArrayList<>();
		logs.add(pos);

		List<BlockPos> leaves = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		// Bamboo, Sugar Cane, Cactus
		BlockState stateAbove = world.getBlockState(pos.up());
		if (isVerticalPlant(stateAbove)) {
			logs.add(pos.up());
			for (int i = 1; i < 256; i++) {
				BlockPos current = pos.up(i);
				if (!isVerticalPlant(world.getBlockState(current)))
					break;
				logs.add(current);
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves);
		}

		// Chorus
		if (isChorus(stateAbove)) {
			frontier.add(pos.up());
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				visited.add(current);
				logs.add(current);
				for (Direction direction : Iterate.directions) {
					BlockPos offset = current.offset(direction);
					if (visited.contains(offset))
						continue;
					if (!isChorus(world.getBlockState(offset)))
						continue;
					frontier.add(offset);
				}
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves);
		}

		// Regular Tree
		if (!validateCut(world, pos))
			return NO_TREE;

		visited.add(pos);
		BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 1, 1))
			.forEach(p -> frontier.add(new BlockPos(p)));

		// Find all logs
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			if (!isLog(world.getBlockState(currentPos)))
				continue;
			logs.add(currentPos);
			addNeighbours(currentPos, frontier, visited);
		}

		// Find all leaves
		visited.clear();
		visited.addAll(logs);
		frontier.addAll(logs);
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (!logs.contains(currentPos) && visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			BlockState blockState = world.getBlockState(currentPos);
			boolean isLog = isLog(blockState);
			boolean isLeaf = isLeaf(blockState);
			boolean isGenericLeaf = isLeaf || isNonDecayingLeaf(blockState);

			if (!isLog && !isGenericLeaf)
				continue;
			if (isGenericLeaf)
				leaves.add(currentPos);

			int distance = !isLeaf ? 0 : blockState.get(LeavesBlock.DISTANCE);
			for (Direction direction : Iterate.directions) {
				BlockPos offset = currentPos.offset(direction);
				if (visited.contains(offset))
					continue;
				BlockState state = world.getBlockState(offset);
				BlockPos subtract = offset.subtract(pos);
				int horizontalDistance = Math.max(Math.abs(subtract.getX()), Math.abs(subtract.getZ()));
				if (isLeaf(state) && state.get(LeavesBlock.DISTANCE) > distance
					|| isNonDecayingLeaf(state) && horizontalDistance < 4)
					frontier.add(offset);
			}

		}

		return new Tree(logs, leaves);
	}

	public static boolean isChorus(BlockState stateAbove) {
		return stateAbove.getBlock() instanceof ChorusPlantBlock || stateAbove.getBlock() instanceof ChorusFlowerBlock;
	}

	public static boolean isVerticalPlant(BlockState stateAbove) {
		Block block = stateAbove.getBlock();
		if (block instanceof BambooBlock)
			return true;
		if (block instanceof CactusBlock)
			return true;
		if (block instanceof SugarCaneBlock)
			return true;
		if (block instanceof KelpBlock)
			return true;
		return block instanceof KelpTopBlock;
	}

	/**
	 * Checks whether a tree was fully cut by seeing whether the layer above the cut
	 * is not supported by any more logs.
	 *
	 * @param reader
	 * @param pos
	 * @return
	 */
	private static boolean validateCut(IBlockReader reader, BlockPos pos) {
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(pos);
		frontier.add(pos.up());
		int posY = pos.getY();

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			visited.add(currentPos);
			boolean lowerLayer = currentPos.getY() == posY;

			if (!isLog(reader.getBlockState(currentPos)))
				continue;
			if (!lowerLayer && !pos.equals(currentPos.down()) && isLog(reader.getBlockState(currentPos.down())))
				return false;

			for (Direction direction : Iterate.directions) {
				if (direction == Direction.DOWN)
					continue;
				if (direction == Direction.UP && !lowerLayer)
					continue;
				BlockPos offset = currentPos.offset(direction);
				if (visited.contains(offset))
					continue;
				frontier.add(offset);
			}

		}

		return true;
	}

	private static void addNeighbours(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited) {
		BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1))
			.filter(((Predicate<BlockPos>) visited::contains).negate())
			.forEach(p -> frontier.add(new BlockPos(p)));
	}

	private static boolean isLog(BlockState state) {
		return state.isIn(BlockTags.LOGS) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state);
	}

	private static boolean isNonDecayingLeaf(BlockState state) {
		return state.isIn(BlockTags.WART_BLOCKS) || state.getBlock() == Blocks.SHROOMLIGHT;
	}

	private static boolean isLeaf(BlockState state) {
		return state.contains(LeavesBlock.DISTANCE);
	}

	public static class Tree extends AbstractBlockBreakQueue {
		private final List<BlockPos> logs;
		private final List<BlockPos> leaves;

		public Tree(List<BlockPos> logs, List<BlockPos> leaves) {
			this.logs = logs;
			this.leaves = leaves;
		}

		@Override
		public void destroyBlocks(World world, ItemStack toDamage, @Nullable PlayerEntity playerEntity,
			BiConsumer<BlockPos, ItemStack> drop) {
			logs.forEach(makeCallbackFor(world, 1 / 2f, toDamage, playerEntity, drop));
			leaves.forEach(makeCallbackFor(world, 1 / 8f, toDamage, playerEntity, drop));
		}
	}
}
