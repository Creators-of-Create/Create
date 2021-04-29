package com.simibubi.create.foundation.render;

import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class PartialBufferer {

	public static SuperByteBuffer get(AllBlockPartials partial, BlockState referenceState) {
		return CreateClient.bufferCache.renderPartial(partial, referenceState);
	}

	public static SuperByteBuffer getDirectionalSouth(AllBlockPartials partial, BlockState referenceState) {
		Direction facing = referenceState.get(FACING);
		return getDirectionalSouth(partial, referenceState, facing);
	}

	public static SuperByteBuffer getDirectional(AllBlockPartials partial, BlockState referenceState) {
		Direction facing = referenceState.get(FACING);
		return getDirectional(partial, referenceState, facing);
	}

	public static SuperByteBuffer getHorizontal(AllBlockPartials partial, BlockState referenceState) {
		Direction facing = referenceState.get(HORIZONTAL_FACING);
		return getDirectionalSouth(partial, referenceState, facing);
	}

	public static SuperByteBuffer getDirectionalSouth(AllBlockPartials partial, BlockState referenceState, Direction facing) {
		return CreateClient.bufferCache.renderDirectionalPartial(partial, referenceState, facing, rotateToFace(facing));
	}

	public static SuperByteBuffer getDirectional(AllBlockPartials partial, BlockState referenceState, Direction facing) {
		Supplier<MatrixStack> ms = () -> {
			MatrixStack stack = new MatrixStack();
			MatrixStacker.of(stack)
					.centre()
					.rotateY(AngleHelper.horizontalAngle(facing))
					.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
					.unCentre();

			return stack;
		};
		return CreateClient.bufferCache.renderDirectionalPartial(partial, referenceState, facing, ms);
	}

	public static Supplier<MatrixStack> rotateToFace(Direction facing) {
		return () -> {
			MatrixStack stack = new MatrixStack();
			MatrixStacker.of(stack)
					.centre()
					.rotateY(AngleHelper.horizontalAngle(facing))
					.rotateX(AngleHelper.verticalAngle(facing))
					.unCentre();
			return stack;
		};
	}

}
