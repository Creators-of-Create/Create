package com.simibubi.create.foundation.utility.placement;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

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
	 *     Use {@link PlacementOffset#success(Vector3i)} with the new BlockPos to indicate a success
	 *     and call {@link PlacementOffset#withTransform(Function)} if the blocks default state has to be modified before it is placed
	 */
	PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray);

	//sets the offset's ghost state with the default state of the held block item, this is used in PlacementHelpers and can be ignored in most cases
	default PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray, ItemStack heldItem) {
		PlacementOffset offset = getOffset(player, world, state, pos, ray);
		if (heldItem.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) heldItem.getItem();
			offset = offset.withGhostState(blockItem.getBlock().getDefaultState());
		}
		return offset;
	}

	/**
	 * overwrite this method if your placement helper needs a different rendering than the default ghost state
	 *
	 * @param pos the position of the Block the player is looking at or clicked on
	 * @param state the Blockstate of the Block that the player is looking at or clicked on
	 * @param ray the exact raytrace result
	 * @param offset the PlacementOffset returned by {@link #getOffset(PlayerEntity, World, BlockState, BlockPos, BlockRayTraceResult)}<br>
	 *               the offset will always be successful if this method is called
	 */
	default void renderAt(BlockPos pos, BlockState state, BlockRayTraceResult ray, PlacementOffset offset) {
		displayGhost(offset);
	}

	//RIP
	static void renderArrow(Vector3d center, Vector3d target, Direction arrowPlane) {
		renderArrow(center, target, arrowPlane, 1D);
	}
	static void renderArrow(Vector3d center, Vector3d target, Direction arrowPlane, double distanceFromCenter) {
		Vector3d direction = target.subtract(center).normalize();
		Vector3d facing = Vector3d.of(arrowPlane.getDirectionVec());
		Vector3d start = center.add(direction);
		Vector3d offset = direction.scale(distanceFromCenter-1);
		Vector3d offsetA = direction.crossProduct(facing).normalize().scale(.25);
		Vector3d offsetB = facing.crossProduct(direction).normalize().scale(.25);
		Vector3d endA = center.add(direction.scale(.75)).add(offsetA);
		Vector3d endB = center.add(direction.scale(.75)).add(offsetB);
		CreateClient.outliner.showLine("placementArrowA" + center + target, start.add(offset), endA.add(offset)).lineWidth(1/16f);
		CreateClient.outliner.showLine("placementArrowB" + center + target, start.add(offset), endB.add(offset)).lineWidth(1/16f);
	}

	default void displayGhost(PlacementOffset offset) {
		if (!offset.hasGhostState())
			return;

		CreateClient.ghostBlocks.showGhostState(this, offset.getTransform().apply(offset.getGhostState()))
				.at(offset.getBlockPos())
				.breathingAlpha();
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vector3d hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() == axis);
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vector3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() == axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vector3d hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() != axis);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vector3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() != axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vector3d hit, Direction.Axis first, Direction.Axis second) {
		return orderedByDistanceExceptAxis(pos, hit, first, d -> d.getAxis() != second);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vector3d hit, Direction.Axis first, Direction.Axis second, Predicate<Direction> includeDirection) {
		return orderedByDistanceExceptAxis(pos, hit, first, ((Predicate<Direction>) d -> d.getAxis() != second).and(includeDirection));
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vector3d hit) {
		return orderedByDistance(pos, hit, _$ -> true);
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vector3d hit, Predicate<Direction> includeDirection) {
		Vector3d centerToHit = hit.subtract(VecHelper.getCenterOf(pos));
		return Arrays.stream(Iterate.directions)
				.filter(includeDirection)
				.map(dir -> Pair.of(dir, Vector3d.of(dir.getDirectionVec()).distanceTo(centerToHit)))
				.sorted(Comparator.comparingDouble(Pair::getSecond))
				.map(Pair::getFirst)
				.collect(Collectors.toList());
	}

	default boolean matchesItem(ItemStack item) {
		return getItemPredicate().test(item);
	}

	default boolean matchesState(BlockState state) {
		return getStatePredicate().test(state);
	}
}
