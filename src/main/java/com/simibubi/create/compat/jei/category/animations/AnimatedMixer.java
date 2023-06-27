package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Mth;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 200);
		matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
		int scale = 23;

		blockElement(cogwheel())
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		blockElement(AllBlocks.MECHANICAL_MIXER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		float animation = ((Mth.sin(AnimationTickHolder.getRenderTime() / 32f) + 1) / 5) + .5f;

		blockElement(AllPartialModels.MECHANICAL_MIXER_POLE)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		blockElement(AllPartialModels.MECHANICAL_MIXER_HEAD)
			.rotateBlock(0, getCurrentAngle() * 4, 0)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		blockElement(AllBlocks.BASIN.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
