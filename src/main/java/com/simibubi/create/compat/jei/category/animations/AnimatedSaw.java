package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 0);
		matrixStack.translate(0, 0, 200);
		matrixStack.translate(2, 22, 0);
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f + 90));
		int scale = 25;

		defaultBlockElement(shaft(Axis.X))
			.rotateBlock(-getCurrentAngle(), 0, 0)
			.scale(scale)
			.render(matrixStack);

		defaultBlockElement(AllBlocks.MECHANICAL_SAW.getDefaultState()
			.with(SawBlock.FACING, Direction.UP))
			.rotateBlock(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		defaultBlockElement(AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE)
			.rotateBlock(0, -90, -90)
			.scale(scale)
			.render(matrixStack);

		matrixStack.pop();
	}

}
