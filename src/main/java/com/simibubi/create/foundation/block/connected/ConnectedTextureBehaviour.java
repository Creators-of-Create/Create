package com.simibubi.create.foundation.block.connected;

import java.util.Map;
import java.util.function.BiPredicate;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ConnectedTextureBehaviour {

	class CTContext {
		boolean up, down, left, right;
		boolean topLeft, topRight, bottomLeft, bottomRight;
	}

	public abstract CTSpriteShiftEntry get(BlockState state, Direction direction);

	boolean shouldFlipUVs(BlockState state, Direction face) {
		return false;
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

		boolean up = connection.test(0, 1);
		boolean down = connection.test(0, -1);
		boolean left = connection.test(-1, 0);
		boolean right = connection.test(1, 0);
		boolean topLeft = connection.test(-1, 1);
		boolean topRight = connection.test(1, 1);
		boolean bottomLeft = connection.test(-1, -1);
		boolean bottomRight = connection.test(1, -1);

		boolean flip = shouldFlipUVs(state, face);
		CTContext context = new CTContext();

		context.up = flip ? down : up;
		context.down = flip ? up : down;
		context.left = flip ? right : left;
		context.right = flip ? left : right;
		context.topLeft = flip ? bottomRight : topLeft;
		context.topRight = flip ? bottomLeft : topRight;
		context.bottomLeft = flip ? topRight : bottomLeft;
		context.bottomRight = flip ? topLeft : bottomRight;

		return context;
	}

}
