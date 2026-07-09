package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import org.joml.Matrix3x2fStack;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 23;

		blockElement(cogwheel())
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.MECHANICAL_MIXER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		float animation = ((Mth.sin(AnimationTickHolder.getRenderTime() / 32f) + 1) / 5) + .5f;

		blockElement(AllPartialModels.MECHANICAL_MIXER_POLE)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllPartialModels.MECHANICAL_MIXER_HEAD)
			.rotateBlock(0, getCurrentAngle() * 4, 0)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		blockElement(AllBlocks.BASIN.getDefaultState())
			.atLocal(0, 1.65f, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
