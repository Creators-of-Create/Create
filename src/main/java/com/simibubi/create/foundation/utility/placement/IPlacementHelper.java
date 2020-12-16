package com.simibubi.create.foundation.utility.placement;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public interface IPlacementHelper {

	/**
	 * @return a predicate that gets tested with the items held in the players hands,
	 * should return true if this placement helper is active with the given item
	 */
	Predicate<ItemStack> getItemPredicate();

	/**
	 * @return a predicate that gets tested with the blockstate the player is looking at
	 * should return true if this placement helper is active with the given blockstate
	 */
	Predicate<BlockState> getStatePredicate();

	/**
	 * @return PlacementOffset.fail() if no valid offset could be found.
	 * PlacementOffset.success(newPos) with newPos being the new position the block should be placed at
	 */
	PlacementOffset getOffset(World world, BlockState state, BlockPos pos, BlockRayTraceResult ray);

	//only gets called when placementOffset is successful
	default void renderAt(BlockPos pos, BlockState state, BlockRayTraceResult ray, PlacementOffset offset) {
		IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), ray.getFace());
	}

	static void renderArrow(Vec3d center, Vec3d target, Direction arrowPlane) {
		renderArrow(center, target, arrowPlane, 1D);
	}
	static void renderArrow(Vec3d center, Vec3d target, Direction arrowPlane, double distanceFromCenter) {
		Vec3d direction = target.subtract(center).normalize();
		Vec3d facing = new Vec3d(arrowPlane.getDirectionVec());
		Vec3d start = center.add(direction);
		Vec3d offset = direction.scale(distanceFromCenter-1);
		Vec3d offsetA = direction.crossProduct(facing).normalize().scale(.25);
		Vec3d offsetB = facing.crossProduct(direction).normalize().scale(.25);
		Vec3d endA = center.add(direction.scale(.75)).add(offsetA);
		Vec3d endB = center.add(direction.scale(.75)).add(offsetB);
		CreateClient.outliner.showLine("placementArrowA" + center + target, start.add(offset), endA.add(offset)).lineWidth(1/16f);
		CreateClient.outliner.showLine("placementArrowB" + center + target, start.add(offset), endB.add(offset)).lineWidth(1/16f);
	}

	/*@OnlyIn(Dist.CLIENT)
	static void renderArrow(Vec3d center, Direction towards, BlockRayTraceResult ray) {
		Direction hitFace = ray.getFace();

		if (hitFace.getAxis() == towards.getAxis())
			return;

		//get the two perpendicular directions to form the arrow
		Direction[] directions = Arrays.stream(Direction.Axis.values()).filter(axis -> axis != hitFace.getAxis() && axis != towards.getAxis()).map(Iterate::directionsInAxis).findFirst().orElse(new Direction[]{});
		Vec3d startOffset = new Vec3d(towards.getDirectionVec());
		Vec3d start = center.add(startOffset);
		for (Direction dir : directions) {
			Vec3d arrowOffset = new Vec3d(dir.getDirectionVec()).scale(.25);
			Vec3d target = center.add(startOffset.scale(0.75)).add(arrowOffset);
			CreateClient.outliner.showLine("placementArrow" + towards + dir, start, target).lineWidth(1/16f);
		}
	}*/

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3d hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() == axis);
	}

	static List<Direction> orderedByDistanceOnlyAxis(BlockPos pos, Vec3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() == axis).and(includeDirection));
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3d hit, Direction.Axis axis) {
		return orderedByDistance(pos, hit, dir -> dir.getAxis() != axis);
	}

	static List<Direction> orderedByDistanceExceptAxis(BlockPos pos, Vec3d hit, Direction.Axis axis, Predicate<Direction> includeDirection) {
		return orderedByDistance(pos, hit, ((Predicate<Direction>) dir -> dir.getAxis() != axis).and(includeDirection));
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3d hit) {
		return orderedByDistance(pos, hit, _$ -> true);
	}

	static List<Direction> orderedByDistance(BlockPos pos, Vec3d hit, Predicate<Direction> includeDirection) {
		Vec3d centerToHit = hit.subtract(VecHelper.getCenterOf(pos));
		return Arrays.stream(Iterate.directions)
				.filter(includeDirection)
				.map(dir -> Pair.of(dir, new Vec3d(dir.getDirectionVec()).distanceTo(centerToHit)))
				.sorted(Comparator.comparingDouble(Pair::getSecond))
				.map(Pair::getFirst)
				.collect(Collectors.toList());
	}
}
