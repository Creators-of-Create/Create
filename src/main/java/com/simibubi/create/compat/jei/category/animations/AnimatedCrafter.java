package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;

public class AnimatedCrafter extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, -16, 13);
		
		matrixStack.translate(3, 16, 0);
		// RenderSystem.rotatef(-12.5f, 1, 0, 0);
		// RenderSystem.rotatef(-22.5f, 0, 1, 0);
		int scale = 22;

		GuiGameElement.of(cogwheel())
				.rotateBlock(90, 0, getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_CRAFTER.getDefaultState())
				.rotateBlock(0, 180, 0)
				.scale(scale)
				.render(matrixStack);

		matrixStack.pop();
	}

}
