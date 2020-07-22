package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public abstract class ConnectedTextureBehaviour {

	public class CTContext {
		boolean up, down, left, right;
		boolean topLeft, topRight, bottomLeft, bottomRight;
	}

	public abstract CTSpriteShiftEntry get(BlockState state, Direction direction);

	protected boolean reverseUVs(BlockState state, Direction face) {
		return false;
	}

	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	public boolean buildContextForOccludedDirections() {
		return false;
	}
	
	public boolean connectsTo(BlockState state, BlockState other, IBlockDisplayReader reader, BlockPos pos, BlockPos otherPos,
		Direction face) {

		BlockPos blockingPos = otherPos.offset(face);
		if ((face.getAxis()
			.getCoordinate(pos.getX(), pos.getY(), pos.getZ()) == face.getAxis()
				.getCoordinate(otherPos.getX(), otherPos.getY(), otherPos.getZ()))
			&& connectsTo(state, reader.getBlockState(blockingPos), reader, pos, blockingPos, face))
			return false;

		return state.getBlock() == other.getBlock();
	}

	public CTContext buildContext(IBlockDisplayReader reader, BlockPos pos, BlockState state, Direction face) {
		CTContext context = new CTContext();
		CTSpriteShiftEntry textureEntry = get(state, face);

		if (textureEntry == null)
			return context;

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

		boolean flipH = reverseUVsHorizontally(state, face);
		boolean flipV = reverseUVsVertically(state, face);
		int sh = flipH ? -1 : 1;
		int sv = flipV ? -1 : 1;

		CTType type = textureEntry.getType();

		if (type != CTType.HORIZONTAL) {
			context.up = testConnection(reader, pos, state, face, horizontal, vertical, 0, sv);
			context.down = testConnection(reader, pos, state, face, horizontal, vertical, 0, -sv);
		}

		if (type != CTType.VERTICAL) {
			context.left = testConnection(reader, pos, state, face, horizontal, vertical, -sh, 0);
			context.right = testConnection(reader, pos, state, face, horizontal, vertical, sh, 0);
		}

		if (type == CTType.OMNIDIRECTIONAL) {
			context.topLeft = testConnection(reader, pos, state, face, horizontal, vertical, -sh, sv);
			context.topRight = testConnection(reader, pos, state, face, horizontal, vertical, sh, sv);
			context.bottomLeft = testConnection(reader, pos, state, face, horizontal, vertical, -sh, -sv);
			context.bottomRight = testConnection(reader, pos, state, face, horizontal, vertical, sh, -sv);
		}

		return context;
	}

	private boolean testConnection(IBlockDisplayReader reader, BlockPos pos, BlockState state, Direction face,
		final Direction horizontal, final Direction vertical, int sh, int sv) {
		BlockPos p = pos.offset(horizontal, sh)
			.offset(vertical, sv);
		boolean test = connectsTo(state, reader.getBlockState(p), reader, pos, p, face);
		return test;
	}

}
