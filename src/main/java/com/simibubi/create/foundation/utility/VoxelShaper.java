package com.simibubi.create.foundation.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class VoxelShaper {

	private Map<Direction, VoxelShape> shapes = new HashMap<>();

	public VoxelShape get(Direction direction) {
		return shapes.get(direction);
	}

	public VoxelShape get(Axis axis) {
		return shapes.get(axisAsFace(axis));
	}

	public static VoxelShaper forHorizontal(VoxelShape shape, Direction facing){
		return forDirectionsWithRotation(shape, facing, Direction.Plane.HORIZONTAL, new HorizontalRotationValues());
	}

	public static VoxelShaper forHorizontalAxis(VoxelShape shape, Axis along) {
		return forDirectionsWithRotation(shape, axisAsFace(along), Arrays.asList(Direction.SOUTH, Direction.EAST), new HorizontalRotationValues());
	}

	public static VoxelShaper forDirectional(VoxelShape shape, Direction facing){
		return forDirectionsWithRotation(shape, facing, Arrays.asList(Iterate.directions), new DefaultRotationValues());
	}

	public static VoxelShaper forAxis(VoxelShape shape, Axis along){
		return forDirectionsWithRotation(shape, axisAsFace(along), Arrays.asList(Direction.SOUTH, Direction.EAST, Direction.UP), new DefaultRotationValues());
	}

	public VoxelShaper withVerticalShapes(VoxelShape upShape) {
		shapes.put(Direction.UP, upShape);
		shapes.put(Direction.DOWN, rotatedCopy(upShape, new Vec3d(180, 0, 0)));
		return this;
	}

	public VoxelShaper withShape(VoxelShape shape, Direction facing){
		shapes.put(facing, shape);
		return this;
	}

	public static Direction axisAsFace(Axis axis) {
		return Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
	}

	protected static float horizontalAngleFromDirection(Direction direction){
		return (float)((Math.max(direction.getHorizontalIndex(), 0) & 3) * 90);
	}

	protected static VoxelShaper forDirectionsWithRotation(VoxelShape shape, Direction facing, Iterable<Direction> directions, Function<Direction, Vec3d> rotationValues){
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction dir : directions) {
			voxelShaper.shapes.put(dir, rotate(shape, facing, dir, rotationValues));
		}
		return voxelShaper;
	}

	protected static VoxelShape rotate(VoxelShape shape, Direction from, Direction to, Function<Direction, Vec3d> usingValues){
		if (from == to)
			return shape;

		return rotatedCopy(shape, usingValues.apply(from).inverse().add(usingValues.apply(to)));
	}

	protected static VoxelShape rotatedCopy(VoxelShape shape, Vec3d rotation){
		if (rotation.equals(Vec3d.ZERO))
			return shape;

		MutableObject<VoxelShape> result = new MutableObject<>(VoxelShapes.empty());
		Vec3d center = new Vec3d(8, 8, 8);

		shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			Vec3d v1 = new Vec3d(x1, y1, z1).scale(16).subtract(center);
			Vec3d v2 = new Vec3d(x2, y2, z2).scale(16).subtract(center);

			v1 = VecHelper.rotate(v1, (float) rotation.x, Axis.X);
			v1 = VecHelper.rotate(v1, (float) rotation.y, Axis.Y);
			v1 = VecHelper.rotate(v1, (float) rotation.z, Axis.Z).add(center);

			v2 = VecHelper.rotate(v2, (float) rotation.x, Axis.X);
			v2 = VecHelper.rotate(v2, (float) rotation.y, Axis.Y);
			v2 = VecHelper.rotate(v2, (float) rotation.z, Axis.Z).add(center);

			VoxelShape rotated = Block.makeCuboidShape(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
			result.setValue(VoxelShapes.or(result.getValue(), rotated));
		});

		return result.getValue();
	}

	protected static class DefaultRotationValues implements Function<Direction, Vec3d> {
		//assume facing up as the default rotation
		@Override
		public Vec3d apply(Direction direction) {
			return new Vec3d(
					direction == Direction.UP ? 0 : (Direction.Plane.VERTICAL.test(direction) ? 180 : 90),
					-horizontalAngleFromDirection(direction),
					0
			);
		}
	}

	protected static class HorizontalRotationValues implements Function<Direction, Vec3d> {
		@Override
		public Vec3d apply(Direction direction) {
			return new Vec3d(
					0,
					-horizontalAngleFromDirection(direction),
					0
			);
		}
	}

}
