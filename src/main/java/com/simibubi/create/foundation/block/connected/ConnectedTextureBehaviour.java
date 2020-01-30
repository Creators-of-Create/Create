package com.simibubi.create.foundation.block.connected;

import java.util.function.BiPredicate;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

public abstract class ConnectedTextureBehaviour {

	class CTContext {
		boolean up, down, left, right;
		boolean topLeft, topRight, bottomLeft, bottomRight;
	}

	public abstract CTSpriteShiftEntry get(BlockState state, Direction direction);

	public abstract Iterable<CTSpriteShiftEntry> getAllCTShifts();

	protected boolean reverseUVs(BlockState state, Direction face) {
		return false;
	}

	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	public boolean connectsTo(BlockState state, BlockState other, IEnviromentBlockReader reader, BlockPos pos,
			BlockPos otherPos, Direction face) {

		BlockPos blockingPos = otherPos.offset(face);
		if ((face.getAxis().getCoordinate(pos.getX(), pos.getY(), pos.getZ()) == face.getAxis()
				.getCoordinate(otherPos.getX(), otherPos.getY(), otherPos.getZ()))
				&& connectsTo(state, reader.getBlockState(blockingPos), reader, pos, blockingPos, face))
			return false;

		return state.getBlock() == other.getBlock();
	}

	CTContext buildContext(IEnviromentBlockReader reader, BlockPos pos, BlockState state, Direction face) {
		Axis axis = face.getAxis();
		boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
		Direction h = axis == Axis.X ? Direction.SOUTH : Direction.WEST;
		Direction v = axis.isHorizontal() ? Direction.UP : Direction.NORTH;
		h = positive ? h.getOpposite() : h;
		if (face == Direction.DOWN) {
			v = v.getOpposite();
			h = h.getOpposite();
		}

		final Direction horizontal = h;
		final Direction vertical = v;

		BiPredicate<Integer, Integer> connection = (x, y) -> {
			BlockPos p = pos.offset(horizontal, x).offset(vertical, y);
			return connectsTo(state, reader.getBlockState(p), reader, pos, p, face);
		};
		
		boolean flipH = reverseUVsHorizontally(state, face);
		boolean flipV = reverseUVsVertically(state, face);
		int sh = flipH ? -1 : 1;
		int sv = flipV ? -1 : 1;

		CTContext context = new CTContext();
		context.up = connection.test(0, sv);
		context.down = connection.test(0, -sv);
		context.left = connection.test(-sh, 0);
		context.right = connection.test(sh, 0);
		context.topLeft = connection.test(-sh, sv);
		context.topRight = connection.test(sh, sv);
		context.bottomLeft = connection.test(-sh, -sv);
		context.bottomRight = connection.test(sh, -sv);

		return context;
	}

}
