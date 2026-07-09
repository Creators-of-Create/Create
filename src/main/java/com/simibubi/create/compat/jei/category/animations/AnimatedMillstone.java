package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.joml.Matrix3x2fStack;

public class AnimatedMillstone extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		AllGuiTextures.JEI_SHADOW.render(graphics, -16, 13);
		matrixStack.translate(-2, 18);
		int scale = 22;

		blockElement(AllPartialModels.MILLSTONE_COG)
			.rotateBlock(22.5, getCurrentAngle() * 2, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.MILLSTONE.getDefaultState())
			.rotateBlock(22.5, 22.5, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
