package com.simibubi.create.foundation.render;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class PartialBufferer {

	public static SuperByteBuffer get(PartialModel partial, BlockState referenceState) {
		return CreateClient.bufferCache.renderPartial(partial, referenceState);
	}

	public static SuperByteBuffer getFacing(PartialModel partial, BlockState referenceState) {
		Direction facing = referenceState.get(FACING);
		return getFacing(partial, referenceState, facing);
	}

	public static SuperByteBuffer getFacing(PartialModel partial, BlockState referenceState, Direction facing) {
		return CreateClient.bufferCache.renderDirectionalPartial(partial, referenceState, facing, rotateToFace(facing));
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
