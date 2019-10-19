package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

public class VoxelShaper {

	private Map<Direction, VoxelShape> shapes = new HashMap<>();

	public VoxelShape get(Direction direction) {
		return shapes.get(direction);
	}

	public static VoxelShaper forHorizontalBlock(VoxelShape southShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction facing : Direction.values()) {
			if (facing.getAxis().isVertical())
				continue;
			voxelShaper.shapes.put(facing, rotatedCopy(southShape, 0, (int) -facing.getHorizontalAngle()));
		}
		return voxelShaper;
	}

	public static VoxelShaper forDirectionalBlock(VoxelShape southShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction facing : Direction.values()) {
			int rotX = facing.getAxis().isVertical() ? (facing == Direction.UP ? 270 : 90) : 0;
			int rotY = facing.getAxis().isVertical() ? 0 : (int) -facing.getHorizontalAngle();
			voxelShaper.shapes.put(facing, rotatedCopy(southShape, rotX, rotY));
		}
		return voxelShaper;
	}

	public VoxelShaper withVerticalShapes(VoxelShape upShape) {
		shapes.put(Direction.UP, upShape);
		shapes.put(Direction.DOWN, rotatedCopy(upShape, 180, 0));
		return this;
	}

	public static VoxelShape rotatedCopy(VoxelShape shape, int rotX, int rotY) {
		Vec3d center = new Vec3d(8, 8, 8);
		Vec3d v1 = new Vec3d(shape.getStart(Axis.X), shape.getStart(Axis.Y), shape.getStart(Axis.Z)).scale(16)
				.subtract(center);
		Vec3d v2 = new Vec3d(shape.getEnd(Axis.X), shape.getEnd(Axis.Y), shape.getEnd(Axis.Z)).scale(16)
				.subtract(center);

		v1 = VecHelper.rotate(v1, rotX, Axis.X);
		v1 = VecHelper.rotate(v1, rotY, Axis.Y).add(center);
		v2 = VecHelper.rotate(v2, rotX, Axis.X);
		v2 = VecHelper.rotate(v2, rotY, Axis.Y).add(center);

		return Block.makeCuboidShape(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
	}

}
