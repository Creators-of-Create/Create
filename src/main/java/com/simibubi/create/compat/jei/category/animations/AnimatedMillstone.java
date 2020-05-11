package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.GuiGameElement;

public class AnimatedMillstone extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 0);
		ScreenResources.JEI_SHADOW.draw(-16, 13);
		RenderSystem.translatef(-2, 18, 0);
		int scale = 22;

		GuiGameElement.of(AllBlockPartials.MILLSTONE_COG)
			.rotateBlock(22.5, getCurrentAngle() * 2, 0)
			.scale(scale)
			.render();
		
		GuiGameElement.of(AllBlocks.MILLSTONE.getDefault())
			.rotateBlock(22.5, 22.5, 0)
			.scale(scale)
			.render();

		RenderSystem.popMatrix();
	}

}
