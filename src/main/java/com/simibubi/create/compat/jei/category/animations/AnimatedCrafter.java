package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.joml.Matrix3x2fStack;

public class AnimatedCrafter extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		AllGuiTextures.JEI_SHADOW.render(graphics, -16, 13);

		matrixStack.translate(3, 16);
		int scale = 22;

		blockElement(cogwheel())
			.rotateBlock(90, 0, getCurrentAngle())
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.MECHANICAL_CRAFTER.getDefaultState())
			.rotateBlock(0, 180, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
