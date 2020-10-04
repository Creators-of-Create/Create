package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 200);
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 23;

		GuiGameElement.of(cogwheel())
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_MIXER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		float animation = ((MathHelper.sin(AnimationTickHolder.getRenderTick() / 32f) + 1) / 5) + .5f;

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_POLE)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_HEAD)
			.rotateBlock(0, getCurrentAngle() * 4, 0)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.pop();
	}

}
