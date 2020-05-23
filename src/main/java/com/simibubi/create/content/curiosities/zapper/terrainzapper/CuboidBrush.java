package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class CuboidBrush extends Brush {

	public static final int MAX_SIZE = 32;
	private VoxelShape shape;
	private List<BlockPos> positions;

	public CuboidBrush() {
		super(3);
		shape = VoxelShapes.empty();
		positions = new ArrayList<>();
	}

	@Override
	public void set(int param0, int param1, int param2) {
		boolean updateShape = this.param0 != param0 || this.param1 != param1 || this.param2 != param2;
		super.set(param0, param1, param2);

		if (updateShape) {
			BlockPos zero = BlockPos.ZERO;
			shape = VoxelShapes.create(new AxisAlignedBB(zero).grow(1 / 32f)
					.grow(((param0 - 1) / 2f), ((param1 - 1) / 2f), ((param2 - 1) / 2f))
					.offset((1 - param0 % 2) * .5f, (1 - param1 % 2) * .5f, (1 - param2 % 2) * .5f));
			positions = BlockPos
					.getAllInBox(zero.add((param0 - 1) / -2, (param1 - 1) / -2, (param2 - 1) / -2),
							zero.add((param0) / 2, (param1) / 2, (param2) / 2))
					.map(BlockPos::new).collect(Collectors.toList());
		}
	}

	@Override
	int getMin(int paramIndex) {
		return 1;
	}

	@Override
	int getMax(int paramIndex) {
		return MAX_SIZE;
	}

	@Override
	public BlockPos getOffset(Vec3d ray, Direction face, PlacementOptions option) {
		if (option == PlacementOptions.Merged)
			return BlockPos.ZERO;

		int offset =
			option == PlacementOptions.Attached ? face.getAxisDirection() == AxisDirection.NEGATIVE ? 2 : 1 : 0;
		int x = (param0 + (param0 == 0 ? 0 : offset)) / 2;
		int y = (param1 + (param1 == 0 ? 0 : offset)) / 2;
		int z = (param2 + (param2 == 0 ? 0 : offset)) / 2;

		return BlockPos.ZERO.offset(face,
				face.getAxis().getCoordinate(x, y, z) * (option == PlacementOptions.Attached ? 1 : -1));
	}

	@Override
	List<BlockPos> getIncludedPositions() {
		return positions;
	}

	@Override
	VoxelShape getSelectionBox() {
		return shape;
	}

}
