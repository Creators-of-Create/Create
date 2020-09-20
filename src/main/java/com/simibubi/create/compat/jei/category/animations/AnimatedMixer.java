package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.MathHelper;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 200);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = 23;

		GuiGameElement.of(cogwheel())
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render();

		GuiGameElement.of(AllBlocks.MECHANICAL_MIXER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render();

		float animation = ((MathHelper.sin(AnimationTickHolder.getRenderTick() / 32f) + 1) / 5) + .5f;

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_POLE)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render();

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_HEAD)
			.rotateBlock(0, getCurrentAngle() * 4, 0)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render();

		GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render();

		RenderSystem.popMatrix();
	}

}
