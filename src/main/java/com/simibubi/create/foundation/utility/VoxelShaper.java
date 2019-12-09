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
		shape = rotateUp(shape, facing);
		return forDirectionsWithRotation(shape, Direction.Plane.HORIZONTAL, new DefaultRotationValues());
	}

	public static VoxelShaper forHorizontalAxis(VoxelShape shape, Direction facing){
		shape = rotateUp(shape, facing);
		return forDirectionsWithRotation(shape, Arrays.asList(Direction.SOUTH, Direction.EAST), new DefaultRotationValues());
	}

	public static VoxelShaper forRotatedPillar(VoxelShape zShape) {//dunno what this was intended for
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Axis axis : Axis.values()) {
			Direction facing = axisAsFace(axis);
			voxelShaper.shapes.put(facing, rotatedCopy(zShape, new Vec3d(0, (int) -facing.getHorizontalAngle(),0)));
		}
		return voxelShaper;
	}

	public static VoxelShaper forDirectional(VoxelShape shape, Direction facing){
		shape = rotateUp(shape, facing);
		return forDirectionsWithRotation(shape, Arrays.asList(Direction.values()), new DefaultRotationValues());
	}

	protected static VoxelShaper forDirectionsWithRotation(VoxelShape shape, Iterable<Direction> directions, Function<Direction, Vec3d> rotationValues){
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction dir : directions) {
			voxelShaper.shapes.put(dir, rotatedCopy(shape, rotationValues.apply(dir)));
		}
		return voxelShaper;
	}

	public VoxelShaper withVerticalShapes(VoxelShape upShape) {
		shapes.put(Direction.UP, upShape);
		shapes.put(Direction.DOWN, rotatedCopy(upShape, new Vec3d(0, 180, 0)));
		return this;
	}

	public VoxelShaper withShape(VoxelShape shape, Direction facing){
		shapes.put(facing, shape);
		return this;
	}

	public static Direction axisAsFace(Axis axis) {
		return Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
	}

	private static VoxelShape rotateUp(VoxelShape shape, Direction facing){
		if (facing != Direction.UP) {
			Vec3d rot = new DefaultRotationValues().apply(facing);
			shape = rotatedCopy(shape, new Vec3d(360, 360,360).subtract(rot));
		}
		return shape;
	}

	protected static VoxelShape rotatedCopy(VoxelShape shape, Vec3d rotation){
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
					Direction.Plane.VERTICAL.test(direction) ? 0 : (int) -direction.getHorizontalAngle(),
					0
			);
		}
	}

}
