package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	private final BlockState wheel = AllBlocks.CRUSHING_WHEEL.getDefaultState()
			.with(BlockStateProperties.AXIS, Axis.X);

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-22.5f));
		int scale = 22;

		defaultBlockElement(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		defaultBlockElement(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render(matrixStack);

		matrixStack.pop();
	}

}
