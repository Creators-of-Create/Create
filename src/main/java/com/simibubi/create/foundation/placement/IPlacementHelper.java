package com.simibubi.create.foundation.placement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@MethodsReturnNonnullByDefault
public interface IPlacementHelper {

	/**
	 * used as an identifier in SuperGlueHandler to skip blocks placed by helpers
	 */
	BlockState ID = new BlockState(Blocks.AIR, null, null);

	/**
	 * @return a predicate that gets tested with the items held in the players hands<br>
	 * should return true if this placement helper is active with the given item
	 */
	Predicate<ItemStack> getItemPredicate();

	/**
	 * @return a predicate that gets tested with the blockstate the player is looking at<br>
	 * should return true if this placement helper is active with the given blockstate
	 */
	Predicate<BlockState> getStatePredicate();

	/**
	 *
	 * @param player the player that activated the placement helper
	 * @param world the world that the placement helper got activated in
	 * @param state the Blockstate of the Block that the player is looking at or clicked on
	 * @param pos the position of the Block the player is looking at or clicked on
	 * @param ray the exact raytrace result
	 *
	 * @return the PlacementOffset object describing where to place the new block.<br>
	 *     Use {@link PlacementOffset#fail} when no new position could be found.<br>
	 *     Use {@link PlacementOffset#success(Vec3i)} with the new BlockPos to indicate a success
	 *     and call {@link PlacementOffset#withTransform(Function)} if the blocks default state has to be modified before it is placed
	 */
	PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray);

	//sets the offset's ghost state with the default state of the held block item, this is used in PlacementHelpers and can be ignored in most cases
	default PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray, ItemStack heldItem) {
		PlacementOffset offset = getOffset(player, world, state, pos, ray);
		if (heldItem.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) heldItem.getItem();
			offset = offset.withGhostState(blockItem.getBlock().defaultBlockState());
		}
		return offset;
	}

	/**
	 * overwrite this method if your placement helper needs a different rendering than the default ghost state
	 *
	 * @param pos the position of the Block the player is looking at or clicked on
	 * @param state the Blockstate of the Block that the player is looking at or clicked on
	 * @param ray the exact raytrace result
	 * @param offset the PlacementOffset returned by {@link #getOffset(Player, Level, BlockState, BlockPos, BlockHitResult)}<br>
	 *               the offset will always be successful if this method is called
	 */
	default void renderAt(BlockPos pos, BlockState state, BlockHitResult ray, PlacementOffset offset) {
		displayGhost(offset);
	}

	//RIP
	static void renderArrow(Vec3 center, Vec3 target, Direction arrowPlane) {
		renderArrow(center, target, arrowPlane, 1D);
	}
	static void renderArrow(Vec3 center, Vec3 target, Direction arrowPlane, double distanceFromCenter) {
		Vec3 direction = target.subtract(center).normalize();
		Vec3 facing = Vec3.atLowerCornerOf(arrowPlane.getNormal());
		Vec3 start = center.add(direction);
		Vec3 offset = direction.scale(distanceFromCenter - 1);
		Vec3 offsetA = direction.cross(facing).normalize().scale(.25);
		Vec3 offsetB = facing.cross(direction).normalize().scale(.25);
		Vec3 endA = center.add(direction.scale(.75)).add(offsetA);
		Vec3 endB = center.add(direction.scale(.75)).add(offsetB);
		CreateClient.OUTLINER.showLine("placementArrowA" + center + target, start.add(offset), endA.add(offset)).lineWidth(1 / 16f);
		CreateClient.OUTLINER.showLine("placementArrowB" + center + target, start.add(offset), endB.add(offset)).lineWidth(1 / 16f);
	}

	default void displayGhost(PlacementOffset offset) {
		if (!offset.hasGhostState())
			return;

		CreateClient.GHOST_BLOCKS.showGhostState(this, offset.getTransform().apply(offset.getGhostState()))
				.at(offset.getBlockPos())
				.breathingAlpha();
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3 hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() == axis);
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3 hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() == axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() != axis);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() != axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis first, Direction.Axis second) {
		return orderedByDistanceExceptAxis(pos, hit, first, d -> d.getAxis() != second);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3 hit, Direction.Axis first, Direction.Axis second, Predicate<Direction> includeDirection) {
		return orderedByDistanceExceptAxis(pos, hit, first, ((Predicate<Direction>) d -> d.getAxis() != second).and(includeDirection));
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit) {
		return orderedByDistance(pos, hit, _$ -> true);
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit, Predicate<Direction> includeDirection) {
		Vec3 centerToHit = hit.subtract(VecHelper.getCenterOf(pos));
		return Arrays.stream(Iterate.directions)
				.filter(includeDirection)
				.map(dir -> Pair.of(dir, Vec3.atLowerCornerOf(dir.getNormal()).distanceTo(centerToHit)))
				.sorted(Comparator.comparingDouble(Pair::getSecond))
				.map(Pair::getFirst)
				.collect(Collectors.toList());
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3 hit, Collection<Direction> directions) {
		Vec3 centerToHit = hit.subtract(VecHelper.getCenterOf(pos));
		return directions.stream()
				.map(dir -> Pair.of(dir, Vec3.atLowerCornerOf(dir.getNormal()).distanceTo(centerToHit)))
				.sorted(Comparator.comparingDouble(Pair::getSecond))
				.map(Pair::getFirst)
				.toList();
	}

	default boolean matchesItem(ItemStack item) {
		return getItemPredicate().test(item);
	}

	default boolean matchesState(BlockState state) {
		return getStatePredicate().test(state);
	}
}
