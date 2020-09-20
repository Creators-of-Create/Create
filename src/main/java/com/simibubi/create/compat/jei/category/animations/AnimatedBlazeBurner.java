package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.gui.GuiGameElement;

import mezz.jei.api.gui.drawable.IDrawable;

public class AnimatedBlazeBurner implements IDrawable {

	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 200);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = 23;

		GuiGameElement.of(AllBlocks.BLAZE_BURNER.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render();

		AllBlockPartials blaze = AllBlockPartials.BLAZES.get(heatLevel);
		GuiGameElement.of(blaze)
			.atLocal(1, 1.65, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render();

		RenderSystem.popMatrix();
	}

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 50;
	}
}
