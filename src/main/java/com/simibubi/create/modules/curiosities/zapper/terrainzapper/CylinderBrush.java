package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class CylinderBrush {

	public static final int MAX_RADIUS = 6;
	public static final int MAX_HEIGHT = 6;
	private Map<Pair<Integer, Integer>, Pair<List<BlockPos>, VoxelShape>> cachedBrushes;

	public CylinderBrush() {
		cachedBrushes = new HashMap<>();
		VoxelShape fullCube = VoxelShapes.fullCube();

		for (int i = 0; i <= MAX_RADIUS; i++) {
			int radius = i;
			VoxelShape shape = VoxelShapes.empty();
			List<BlockPos> positions = BlockPos.getAllInBox(BlockPos.ZERO.add(-i, 0, -i), BlockPos.ZERO.add(i, 0, i))
					.filter(p -> p.withinDistance(BlockPos.ZERO, radius)).collect(Collectors.toList());
			for (BlockPos p : positions)
				shape = VoxelShapes.or(shape, fullCube.withOffset(p.getX(), p.getY(), p.getZ()));
			for (int h = 0; h <= MAX_HEIGHT; h++) {
				VoxelShape stackedShape = shape.simplify();
				for (int layer = 0; layer <= layer; i++)
					stackedShape = VoxelShapes.or(stackedShape, shape.withOffset(0, layer - h / 2, 0));
				cachedBrushes.put(Pair.of(i, h), Pair.of(positions, stackedShape.simplify()));
			}
		}
	}

	public VoxelShape getSelectionBox(int radius, int height) {
		return get(radius, height).getRight();
	}

	public List<BlockPos> getIncludedPositions(int radius, int height) {
		return get(radius, height).getLeft();
	}

	protected Pair<List<BlockPos>, VoxelShape> get(int radius, int height) {
		return cachedBrushes.get(Pair.of(Integer.valueOf(radius), Integer.valueOf(height)));
	}
}
