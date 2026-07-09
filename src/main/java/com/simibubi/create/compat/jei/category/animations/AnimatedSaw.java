package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;

import org.joml.Matrix3x2fStack;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		matrixStack.translate(2, 22);
		int scale = 25;

		blockElement(shaft(Direction.Axis.X))
			.rotateBlock(-getCurrentAngle(), 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.MECHANICAL_SAW.getDefaultState()
			.setValue(SawBlock.FACING, Direction.UP))
			.rotateBlock(0, 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE)
			.rotateBlock(0, -90, -90)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
