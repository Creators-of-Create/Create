package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.GuiGameElement;

public class AnimatedCrafter extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 0);
		ScreenResources.JEI_SHADOW.draw(-16, 13);
		
		RenderSystem.translatef(3, 16, 0);
		RenderSystem.rotatef(-12.5f, 1, 0, 0);
		RenderSystem.rotatef(-22.5f, 0, 1, 0);
		int scale = 22;

		GuiGameElement.of(cogwheel())
				.rotateBlock(90, 0, getCurrentAngle())
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlocks.MECHANICAL_CRAFTER.getDefaultState())
				.rotateBlock(0, 180, 0)
				.scale(scale)
				.render();

		RenderSystem.popMatrix();
	}

}
