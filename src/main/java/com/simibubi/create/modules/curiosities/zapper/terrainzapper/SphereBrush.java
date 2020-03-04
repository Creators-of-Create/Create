package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class SphereBrush {

	public static final int MAX_RADIUS = 6;
	private Map<Integer, Pair<List<BlockPos>, VoxelShape>> cachedBrushes;

	public SphereBrush() {
		cachedBrushes = new HashMap<>();
		for (int i = 0; i <= MAX_RADIUS; i++) {
			int radius = i;
			VoxelShape shape = VoxelShapes.empty();
			List<BlockPos> positions = BlockPos.getAllInBox(BlockPos.ZERO.add(-i, -i, -i), BlockPos.ZERO.add(i, i, i))
					.filter(p -> p.withinDistance(BlockPos.ZERO, radius)).collect(Collectors.toList());
			VoxelShape fullCube = VoxelShapes.fullCube();
			for (BlockPos p : positions)
				shape = VoxelShapes.or(shape, fullCube.withOffset(p.getX(), p.getY(), p.getZ()));
			shape = shape.simplify();
			cachedBrushes.put(i, Pair.of(positions, shape));
		}
	}

	public VoxelShape getSelectionBox(int size) {
		return get(size).getRight();
	}

	public List<BlockPos> getIncludedPositions(int size) {
		return get(size).getLeft();
	}

	protected Pair<List<BlockPos>, VoxelShape> get(int size) {
		return cachedBrushes.get(Integer.valueOf(size));
	}

}
