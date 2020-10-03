package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Quaternion;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, -16, 13);

		matrixStack.translate(0, 0, 200);
		matrixStack.translate(-6, 19, 0);
		matrixStack.multiply(new Quaternion( -22.5f, 1, 0, 0));
		matrixStack.multiply(new Quaternion(90 - 22.5f, 0, 1, 0));
		int scale = 25;

		GuiGameElement.of(shaft(Axis.X))
				.rotateBlock(-getCurrentAngle(), 0, 0)
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_SAW.getDefaultState()
				.with(SawBlock.FACING, Direction.UP)
				.with(SawBlock.RUNNING, true))
				.rotateBlock(0, 0, 0)
				.scale(scale)
				.render(matrixStack);

		matrixStack.pop();
	}

}
