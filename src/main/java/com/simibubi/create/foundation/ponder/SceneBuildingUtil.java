package com.simibubi.create.foundation.ponder;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

/**
 * Helpful shortcuts for marking boundaries, points or sections inside the scene
 */
public class SceneBuildingUtil {

	public final SelectionUtil select;
	public final VectorUtil vector;
	public final PositionUtil grid;

	private final BoundingBox sceneBounds;

	SceneBuildingUtil(BoundingBox sceneBounds) {
		this.sceneBounds = sceneBounds;
		this.select = new SelectionUtil();
		this.vector = new VectorUtil();
		this.grid = new PositionUtil();
	}

	public class PositionUtil {

		public BlockPos at(int x, int y, int z) {
			return new BlockPos(x, y, z);
		}

		public BlockPos zero() {
			return at(0, 0, 0);
		}

	}

	public class VectorUtil {

		public Vec3 centerOf(int x, int y, int z) {
			return centerOf(grid.at(x, y, z));
		}

		public Vec3 centerOf(BlockPos pos) {
			return VecHelper.getCenterOf(pos);
		}

		public Vec3 topOf(int x, int y, int z) {
			return blockSurface(grid.at(x, y, z), Direction.UP);
		}

		public Vec3 topOf(BlockPos pos) {
			return blockSurface(pos, Direction.UP);
		}

		public Vec3 blockSurface(BlockPos pos, Direction face) {
			return blockSurface(pos, face, 0);
		}

		public Vec3 blockSurface(BlockPos pos, Direction face, float margin) {
			return centerOf(pos).add(Vec3.atLowerCornerOf(face.getNormal())
				.scale(.5f + margin));
		}

		public Vec3 of(double x, double y, double z) {
			return new Vec3(x, y, z);
		}

	}

	public class SelectionUtil {

		public Selection everywhere() {
			return Selection.of(sceneBounds);
		}

		public Selection position(int x, int y, int z) {
			return position(grid.at(x, y, z));
		}

		public Selection position(BlockPos pos) {
			return cuboid(pos, BlockPos.ZERO);
		}

		public Selection fromTo(int x, int y, int z, int x2, int y2, int z2) {
			return fromTo(new BlockPos(x, y, z), new BlockPos(x2, y2, z2));
		}

		public Selection fromTo(BlockPos pos1, BlockPos pos2) {
			return cuboid(pos1, pos2.subtract(pos1));
		}

		public Selection column(int x, int z) {
			return cuboid(new BlockPos(x, 1, z), new Vec3i(0, sceneBounds.getYSpan(), 0));
		}

		public Selection layer(int y) {
			return layers(y, 1);
		}

		public Selection layersFrom(int y) {
			return layers(y, sceneBounds.getYSpan() - y);
		}

		public Selection layers(int y, int height) {
			return cuboid(new BlockPos(0, y, 0), new Vec3i(sceneBounds.getXSpan() - 1,
				Math.min(sceneBounds.getYSpan() - y, height) - 1, sceneBounds.getZSpan() - 1));
		}

		public Selection cuboid(BlockPos origin, Vec3i size) {
			return Selection.of(BoundingBox.fromCorners(origin, origin.offset(size)));
		}

	}

}