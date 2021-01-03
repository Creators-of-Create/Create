package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicates;
import com.simibubi.create.AllTags;

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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class TreeCutter {

	public static class Tree {
		public List<BlockPos> logs;
		public List<BlockPos> leaves;

		public Tree(List<BlockPos> logs, List<BlockPos> leaves) {
			this.logs = logs;
			this.leaves = leaves;
		}
	}

	/**
	 * Finds a tree at the given pos. Block at the position should be air
	 * 
	 * @param reader
	 * @param pos
	 * @return null if not found or not fully cut
	 */
	public static Tree cutTree(IBlockReader reader, BlockPos pos) {
		List<BlockPos> logs = new ArrayList<>();
		List<BlockPos> leaves = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		// Bamboo, Sugar Cane, Cactus
		BlockState stateAbove = reader.getBlockState(pos.up());
		if (isVerticalPlant(stateAbove)) {
			logs.add(pos.up());
			for (int i = 1; i < 256; i++) {
				BlockPos current = pos.up(i);
				if (!isVerticalPlant(reader.getBlockState(current)))
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
					if (!isChorus(reader.getBlockState(offset)))
						continue;
					frontier.add(offset);
				}
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves);
		}

		// Regular Tree
		if (!validateCut(reader, pos))
			return null;

		visited.add(pos);
		BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 1, 1))
			.forEach(p -> frontier.add(new BlockPos(p)));

		// Find all logs
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			if (!isLog(reader.getBlockState(currentPos)))
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
			if (!logs.contains(currentPos))
				if (visited.contains(currentPos))
					continue;
			visited.add(currentPos);

			BlockState blockState = reader.getBlockState(currentPos);
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
				BlockState state = reader.getBlockState(offset);
				BlockPos subtract = offset.subtract(pos);
				int horizontalDistance = Math.max(Math.abs(subtract.getX()), Math.abs(subtract.getZ()));
				if (isLeaf(state) && state.get(LeavesBlock.DISTANCE) > distance || isNonDecayingLeaf(state) && horizontalDistance < 4)
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
		if (block instanceof KelpTopBlock)
			return true;
		return false;
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
			.filter(Predicates.not(visited::contains))
			.forEach(p -> frontier.add(new BlockPos(p)));
	}

	private static boolean isLog(BlockState state) {
		return state.isIn(BlockTags.LOGS) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state);
	}

	private static boolean isNonDecayingLeaf(BlockState state) {
		return state.isIn(BlockTags.WART_BLOCKS) || state.getBlock() == Blocks.SHROOMLIGHT;
	}

	private static boolean isLeaf(BlockState state) {
		return BlockHelper.hasBlockStateProperty(state, LeavesBlock.DISTANCE);
	}

}
