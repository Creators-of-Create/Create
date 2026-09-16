package com.simibubi.create.content.kinetics.saw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.dynamictrees.DynamicTree;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class TreeCutter {

	public static final Tree NO_TREE =
		new Tree(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

	public static boolean canDynamicTreeCutFrom(Block startBlock) {
		return Mods.DYNAMICTREES.runIfInstalled(() -> () -> DynamicTree.isDynamicBranch(startBlock))
			.orElse(false);
	}

	@NotNull
	public static Optional<AbstractBlockBreakQueue> findDynamicTree(Block startBlock, BlockPos pos) {
		if (canDynamicTreeCutFrom(startBlock))
			return Mods.DYNAMICTREES.runIfInstalled(() -> () -> new DynamicTree(pos));
		return Optional.empty();
	}

	/**
	 * Finds a tree at the given pos. Block at the position should be air
	 *
	 * @param reader      the level that will be searched for a tree
	 * @param pos         position that the saw cut at
	 * @param brokenState block state what was broken by the saw
	 */
	@NotNull
	public static Tree findTree(@Nullable BlockGetter reader, BlockPos pos, BlockState brokenState) {
		if (reader == null)
			return NO_TREE;

		List<BlockPos> logs = new ArrayList<>();
		List<BlockPos> leaves = new ArrayList<>();
		List<BlockPos> attachments = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		BlockState stateAbove = reader.getBlockState(pos.above());
		// Bamboo, Sugar Cane, Cactus
		if (isVerticalPlant(brokenState)) {
			if (!isVerticalPlant(stateAbove))
				return NO_TREE;

			logs.add(pos.above());
			for (int i = 1; i < reader.getHeight(); i++) {
				BlockPos current = pos.above(i);
				if (!isVerticalPlant(reader.getBlockState(current)))
					break;
				logs.add(current);
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves, attachments);
		}

		// Chorus
		if (isChorus(brokenState)) {
			if (!isChorus(stateAbove))
				return NO_TREE;

			frontier.add(pos.above());
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				visited.add(current);
				logs.add(current);
				for (Direction direction : Iterate.directions) {
					BlockPos offset = current.relative(direction);
					if (visited.contains(offset))
						continue;
					if (!isChorus(reader.getBlockState(offset)))
						continue;
					frontier.add(offset);
				}
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves, attachments);
		}

		// Regular Tree
		if (!validateCut(reader, pos))
			return NO_TREE;

		visited.add(pos);
		BlockPos.betweenClosedStream(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))
			.forEach(p -> frontier.add(new BlockPos(p)));

		// Find all logs & roots
		boolean hasRoots = false;
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (!visited.add(currentPos))
				continue;

			BlockState currentState = reader.getBlockState(currentPos);
			if (isRoot(currentState))
				hasRoots = true;
			else if (!isLog(currentState))
				continue;
			logs.add(currentPos);
			forNeighbours(currentPos, visited, SearchDirection.UP, p -> frontier.add(new BlockPos(p)));
		}

		visited.clear();
		visited.addAll(logs);
		frontier.addAll(logs);

		if (hasRoots) {
			Set<BlockPos> oldLogs = new HashSet<>(logs);
			while (!frontier.isEmpty()) {
				BlockPos currentPos = frontier.remove(0);

				BlockState currentState = reader.getBlockState(currentPos);
				if (!isRoot(currentState))
					continue;
				if (!oldLogs.contains(currentPos))
					logs.add(currentPos);
				forNeighbours(currentPos, visited, SearchDirection.DOWN, p -> {
					BlockPos neighbourPos = p.immutable();
					if (visited.add(neighbourPos))
						frontier.add(neighbourPos);
				});
			}

			visited.clear();
			visited.addAll(logs);
			frontier.addAll(logs);
		}

		// Find all leaves
		while (!frontier.isEmpty()) {
			BlockPos prevPos = frontier.remove(0);

			BlockState prevState = reader.getBlockState(prevPos);
			int prevLeafDistance = isLeaf(prevState) ? getLeafDistance(prevState) : 0;

			forNeighbours(prevPos, visited, SearchDirection.BOTH, currentPos -> {
				BlockState state = reader.getBlockState(currentPos);
				BlockPos subtract = currentPos.subtract(pos);
				BlockPos currentPosImmutable = currentPos.immutable();

				if (AllBlockTags.TREE_ATTACHMENTS.matches(state)) {
					attachments.add(currentPosImmutable);
					visited.add(currentPosImmutable);
					return;
				}

				int horizontalDistance = Math.max(Math.abs(subtract.getX()), Math.abs(subtract.getZ()));
				if (horizontalDistance <= nonDecayingLeafDistance(state) && visited.add(currentPosImmutable)) {
					leaves.add(currentPosImmutable);
					frontier.add(currentPosImmutable);
					return;
				}

				if (isLeaf(state) && getLeafDistance(state) > prevLeafDistance && visited.add(currentPosImmutable)) {
					leaves.add(currentPosImmutable);
					frontier.add(currentPosImmutable);
					return;
				}

			});
		}
		return new Tree(logs, leaves, attachments);
	}

	private static int getLeafDistance(BlockState state) {
		IntegerProperty distanceProperty = LeavesBlock.DISTANCE;
		for (Property<?> property : state.getValues()
			.keySet())
			if (property instanceof IntegerProperty ip && property.getName()
				.equals("distance"))
				distanceProperty = ip;
		return state.getValue(distanceProperty);
	}

	public static boolean isChorus(BlockState stateAbove) {
		return stateAbove.getBlock() instanceof ChorusPlantBlock || stateAbove.getBlock() instanceof ChorusFlowerBlock;
	}

	public static boolean isVerticalPlant(BlockState stateAbove) {
		return AllBlockTags.VERTICAL_PLANTS.matches(stateAbove);
	}

	/**
	 * Checks whether a tree was fully cut by seeing whether the layer above the cut
	 * is not supported by any more logs.
	 *
	 * @param reader
	 * @param pos
	 * @return
	 */
	private static boolean validateCut(BlockGetter reader, BlockPos pos) {
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(pos);
		frontier.add(pos.above());
		int posY = pos.getY();

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			BlockPos belowPos = currentPos.below();

			visited.add(currentPos);
			boolean lowerLayer = currentPos.getY() == posY;

			BlockState currentState = reader.getBlockState(currentPos);
			BlockState belowState = reader.getBlockState(belowPos);

			if (!isLog(currentState) && !isRoot(currentState))
				continue;
			if (!lowerLayer && !pos.equals(belowPos) && (isLog(belowState) || isRoot(belowState)))
				return false;

			for (Direction direction : Iterate.directions) {
				if (direction == Direction.DOWN)
					continue;
				if (direction == Direction.UP && !lowerLayer)
					continue;
				BlockPos offset = currentPos.relative(direction);
				if (visited.contains(offset))
					continue;
				frontier.add(offset);
			}

		}

		return true;
	}

	private enum SearchDirection {
		UP(0, 1), DOWN(-1, 0), BOTH(-1, 1);

		int minY;
		int maxY;

		private SearchDirection(int minY, int maxY) {
			this.minY = minY;
			this.maxY = maxY;
		}
	}

	private static void forNeighbours(BlockPos pos, Set<BlockPos> visited, SearchDirection direction,
									  Consumer<BlockPos> acceptor) {
		BlockPos.betweenClosedStream(pos.offset(-1, direction.minY, -1), pos.offset(1, direction.maxY, 1))
			.filter(((Predicate<BlockPos>) visited::contains).negate())
			.forEach(acceptor);
	}

	public static boolean isRoot(BlockState state) {
		return AllBlockTags.ROOTS.matches(state);
	}

	public static boolean isLog(BlockState state) {
		return state.is(BlockTags.LOGS) || AllBlockTags.SLIMY_LOGS.matches(state)
			|| AllBlockTags.MUSHROOM_STEMS.matches(state);
	}

	private static int nonDecayingLeafDistance(BlockState state) {
		if (AllBlockTags.NON_DECAYING_LEAF_DIST4.matches(state)) {
			return 4;
		}
		if (AllBlockTags.NON_DECAYING_LEAF_DIST3.matches(state)){
			return 3;
		}
		if (AllBlockTags.NON_DECAYING_LEAF_DIST2.matches(state)){
			return 2;
		}
		if (AllBlockTags.NON_DECAYING_LEAF_DIST1.matches(state)){
			return 1;
		}
		return -1;
	}

	private static boolean isLeaf(BlockState state) {
		for (Property<?> property : state.getValues().keySet())
			if (property instanceof IntegerProperty && property.getName().equals("distance") && property != BlockStateProperties.STABILITY_DISTANCE)
				return true;
		return false;
	}

	public static class Tree extends AbstractBlockBreakQueue {
		private final List<BlockPos> logs;
		private final List<BlockPos> leaves;
		private final List<BlockPos> attachments;

		public Tree(List<BlockPos> logs, List<BlockPos> leaves, List<BlockPos> attachments) {
			this.logs = logs;
			this.leaves = leaves;
			this.attachments = attachments;
		}

		@Override
		public void destroyBlocks(Level world, ItemStack toDamage, @Nullable Player playerEntity,
								  BiConsumer<BlockPos, ItemStack> drop) {
			attachments.forEach(makeCallbackFor(world, 1 / 32f, toDamage, playerEntity, drop));
			logs.forEach(makeCallbackFor(world, 1 / 2f, toDamage, playerEntity, drop));
			leaves.forEach(makeCallbackFor(world, 1 / 8f, toDamage, playerEntity, drop));
		}
	}
}
