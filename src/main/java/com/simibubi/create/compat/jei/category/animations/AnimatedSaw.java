package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 0);
		ScreenResources.JEI_SHADOW.draw(-16, 13);
		
		RenderSystem.translatef(0, 0, 200);
		RenderSystem.translatef(-6, 19, 0);
		RenderSystem.rotatef(-22.5f, 1, 0, 0);
		RenderSystem.rotatef(90 - 22.5f, 0, 1, 0);
		int scale = 25;

		GuiGameElement.of(shaft(Axis.X))
				.rotateBlock(-getCurrentAngle(), 0, 0)
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlocksNew.SAW.getDefaultState()
				.with(SawBlock.FACING, Direction.UP)
				.with(SawBlock.RUNNING, true))
				.rotateBlock(0, 0, 0)
				.scale(scale)
				.render();

		RenderSystem.popMatrix();
	}

}
