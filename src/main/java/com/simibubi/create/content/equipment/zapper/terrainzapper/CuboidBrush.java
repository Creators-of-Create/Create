package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

public class CuboidBrush extends ShapedBrush {

	public static final int MAX_SIZE = 32;
	private List<BlockPos> positions;

	public CuboidBrush() {
		super(3);
		positions = new ArrayList<>();
	}

	@Override
	public void set(int param0, int param1, int param2) {
		boolean updateShape = this.param0 != param0 || this.param1 != param1 || this.param2 != param2;
		super.set(param0, param1, param2);
		if (updateShape) {
			BlockPos zero = BlockPos.ZERO;
			positions = BlockPos
				.betweenClosedStream(zero.offset((param0 - 1) / -2, (param1 - 1) / -2, (param2 - 1) / -2),
					zero.offset((param0) / 2, (param1) / 2, (param2) / 2))
				.map(BlockPos::new)
				.collect(Collectors.toList());
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
	public BlockPos getOffset(Vec3 ray, Direction face, PlacementOptions option) {
		if (option == PlacementOptions.Merged)
			return BlockPos.ZERO;

		int offset =
			option == PlacementOptions.Attached ? face.getAxisDirection() == AxisDirection.NEGATIVE ? 2 : 1 : 0;
		int x = (param0 + (param0 == 0 ? 0 : offset)) / 2;
		int y = (param1 + (param1 == 0 ? 0 : offset)) / 2;
		int z = (param2 + (param2 == 0 ? 0 : offset)) / 2;

		return BlockPos.ZERO.relative(face, face.getAxis()
			.choose(x, y, z) * (option == PlacementOptions.Attached ? 1 : -1));
	}

	@Override
	List<BlockPos> getIncludedPositions() {
		return positions;
	}

}
