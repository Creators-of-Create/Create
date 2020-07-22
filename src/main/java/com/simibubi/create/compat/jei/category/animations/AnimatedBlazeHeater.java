package com.simibubi.create.compat.jei.category.animations;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;

import mezz.jei.api.gui.drawable.IDrawable;

public class AnimatedBlazeHeater implements IDrawable {
	private static final HashMap<Integer, AllBlockPartials> blazeModelMap = new HashMap<>();
	
	public AnimatedBlazeHeater() {
		super();
		blazeModelMap.put(2, AllBlockPartials.BLAZE_HEATER_BLAZE_TWO);
		blazeModelMap.put(3, AllBlockPartials.BLAZE_HEATER_BLAZE_THREE);
		blazeModelMap.put(4, AllBlockPartials.BLAZE_HEATER_BLAZE_FOUR);
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		drawWithHeatLevel(xOffset, yOffset, 3);
	}
	
	public void drawWithHeatLevel(int xOffset, int yOffset, int heatLevel) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 200);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = 23;

		GuiGameElement.of(AllBlocks.HEATER.getDefaultState())
				.atLocal(0, 1.65, 0)
				.scale(scale)
				.render();
		
		GuiGameElement.of(blazeModelMap.getOrDefault(heatLevel, AllBlockPartials.BLAZE_HEATER_BLAZE_ONE))
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
