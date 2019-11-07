package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicates;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
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

		if (!validateCut(reader, pos))
			return null;

		visited.add(pos);
		addNeighbours(pos, frontier, visited);

		// Find all logs
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
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
			visited.add(currentPos);

			BlockState blockState = reader.getBlockState(currentPos);
			boolean isLog = !isLog(blockState);
			boolean isLeaf = !isLeaf(blockState);

			if (!isLog && !isLeaf)
				continue;
			if (isLeaf)
				leaves.add(currentPos);

			int distance = isLog ? 0 : blockState.get(LeavesBlock.DISTANCE);
			for (Direction direction : Direction.values()) {
				BlockPos offset = currentPos.offset(direction);
				if (visited.contains(offset))
					continue;
				BlockState state = reader.getBlockState(offset);
				if (isLeaf(state) && state.get(LeavesBlock.DISTANCE) > distance)
					frontier.add(offset);
			}

		}

		return new Tree(logs, leaves);
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
		frontier.add(pos.up());

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			visited.add(currentPos);

			if (!isLog(reader.getBlockState(currentPos)))
				continue;
			if (isLog(reader.getBlockState(currentPos.down())))
				return false;

			for (Direction direction : Direction.values()) {
				if (direction.getAxis().isVertical())
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
		BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1)).filter(Predicates.not(visited::contains))
				.forEach(frontier::add);
	}

	private static boolean isLog(BlockState state) {
		return state.isIn(BlockTags.LOGS);
	}

	private static boolean isLeaf(BlockState state) {
		return state.has(LeavesBlock.DISTANCE);
	}

}
