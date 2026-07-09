package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;

import org.joml.Matrix3x2fStack;

public class AnimatedPress extends AnimatedKinetics {

	private boolean basin;

	public AnimatedPress(boolean basin) {
		this.basin = basin;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = basin ? 23 : 24;

		blockElement(shaft(Direction.Axis.Z))
				.rotateBlock(0, 0, getCurrentAngle())
				.scale(scale)
				.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.MECHANICAL_PRESS.getDefaultState())
				.scale(scale)
				.render(graphics, 0, 0, 0);

		blockElement(AllPartialModels.MECHANICAL_PRESS_HEAD)
				.atLocal(0, -getAnimatedHeadOffset(), 0)
				.scale(scale)
				.render(graphics, 0, 0, 0);

		if (basin)
			blockElement(AllBlocks.BASIN.getDefaultState())
					.atLocal(0, 1.65f, 0)
					.scale(scale)
					.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

	private float getAnimatedHeadOffset() {
		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		if (cycle < 10) {
			float progress = cycle / 10;
			return -(progress * progress * progress);
		}
		if (cycle < 15)
			return -1;
		if (cycle < 20)
			return -1 + (1 - ((20 - cycle) / 5));
		return 0;
	}

}
