package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class CylinderBrush extends Brush {

	public static final int MAX_RADIUS = 6;
	public static final int MAX_HEIGHT = 8;
	private Map<Pair<Integer, Integer>, Pair<List<BlockPos>, VoxelShape>> cachedBrushes;

	public CylinderBrush() {
		super(2);

		cachedBrushes = new HashMap<>();
		VoxelShape fullCube = Block.makeCuboidShape(-.5f, -.5f, -.5f, 16.5f, 16.5f, 16.5f);

		for (int i = 0; i <= MAX_RADIUS; i++) {
			int radius = i;
			VoxelShape shape = VoxelShapes.empty();
			List<BlockPos> positions =
				BlockPos.getAllInBox(BlockPos.ZERO.add(-i - 1, 0, -i - 1), BlockPos.ZERO.add(i + 1, 0, i + 1))
						.map(BlockPos::new).filter(p -> VecHelper.getCenterOf(p)
								.distanceTo(VecHelper.getCenterOf(BlockPos.ZERO)) < radius + .42f)
						.collect(Collectors.toList());
			for (BlockPos p : positions)
				shape = VoxelShapes.or(shape, fullCube.withOffset(p.getX(), p.getY(), p.getZ()));
			for (int h = 0; h <= MAX_HEIGHT; h++) {
				List<BlockPos> stackedPositions = new ArrayList<>();
				VoxelShape stackedShape = shape.simplify();
				for (int layer = 0; layer < h; layer++) {
					int yOffset = layer - h / 2;
					stackedShape = VoxelShapes.or(stackedShape, shape.withOffset(0, yOffset, 0));
					for (BlockPos p : positions)
						stackedPositions.add(p.up(yOffset));
				}
				cachedBrushes.put(Pair.of(i, h), Pair.of(stackedPositions, stackedShape.simplify()));
			}
		}
	}

	@Override
	public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
		if (option == PlacementOptions.Merged)
			return BlockPos.ZERO;

		int offset = option == PlacementOptions.Attached ? 0 : -1;
		boolean negative = face.getAxisDirection() == AxisDirection.NEGATIVE;
		int yOffset = option == PlacementOptions.Attached ? negative ? 1 : 2 : negative ? 0 : -1;
		int r = (param0 + 1 + offset);
		int y = (param1 + (param1 == 0 ? 0 : yOffset)) / 2;

		return BlockPos.ZERO.offset(face,
				(face.getAxis().isVertical() ? y : r) * (option == PlacementOptions.Attached ? 1 : -1));
	}

	@Override
	int getMax(int paramIndex) {
		return paramIndex == 0 ? MAX_RADIUS : MAX_HEIGHT;
	}

	@Override
	int getMin(int paramIndex) {
		return paramIndex == 0 ? 0 : 1;
	}

	@Override
	String getParamLabel(int paramIndex) {
		return paramIndex == 0 ? Lang.translate("generic.radius") : super.getParamLabel(paramIndex);
	}

	@Override
	VoxelShape getSelectionBox() {
		return getEntry(param0, param1).getRight();
	}

	@Override
	public List<BlockPos> getIncludedPositions() {
		return getEntry(param0, param1).getLeft();
	}

	protected Pair<List<BlockPos>, VoxelShape> getEntry(int radius, int height) {
		return cachedBrushes.get(Pair.of(Integer.valueOf(radius), Integer.valueOf(height)));
	}

}
