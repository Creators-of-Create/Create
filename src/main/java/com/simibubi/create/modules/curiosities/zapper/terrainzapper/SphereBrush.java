package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class SphereBrush extends Brush {

	public static final int MAX_RADIUS = 6;
	private Map<Integer, Pair<List<BlockPos>, VoxelShape>> cachedBrushes;

	public SphereBrush() {
		super(1);

		cachedBrushes = new HashMap<>();
		for (int i = 0; i <= MAX_RADIUS; i++) {
			int radius = i;
			VoxelShape shape = VoxelShapes.empty();
			List<BlockPos> positions =
				BlockPos.getAllInBox(BlockPos.ZERO.add(-i - 1, -i - 1, -i - 1), BlockPos.ZERO.add(i + 1, i + 1, i + 1))
						.map(BlockPos::new).filter(p -> VecHelper.getCenterOf(p)
								.distanceTo(VecHelper.getCenterOf(BlockPos.ZERO)) < radius + .5f)
						.collect(Collectors.toList());
			VoxelShape fullCube = Block.makeCuboidShape(-.5f, -.5f, -.5f, 16.5f, 16.5f, 16.5f);
			for (BlockPos p : positions)
				shape = VoxelShapes.or(shape, fullCube.withOffset(p.getX(), p.getY(), p.getZ()));
			shape = shape.simplify();
			cachedBrushes.put(i, Pair.of(positions, shape));
		}
	}

	@Override
	public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
		if (option == PlacementOptions.Merged)
			return BlockPos.ZERO;

		int offset = option == PlacementOptions.Attached ? 0 : -1;
		int r = (param0 + 1 + offset);

		return BlockPos.ZERO.offset(face, r * (option == PlacementOptions.Attached ? 1 : -1));
	}

	@Override
	int getMax(int paramIndex) {
		return MAX_RADIUS;
	}

	@Override
	VoxelShape getSelectionBox() {
		return getEntry(param0).getRight();
	}

	@Override
	String getParamLabel(int paramIndex) {
		return Lang.translate("generic.radius");
	}

	@Override
	List<BlockPos> getIncludedPositions() {
		return getEntry(param0).getLeft();
	}

	protected Pair<List<BlockPos>, VoxelShape> getEntry(int size) {
		return cachedBrushes.get(Integer.valueOf(size));
	}

}
