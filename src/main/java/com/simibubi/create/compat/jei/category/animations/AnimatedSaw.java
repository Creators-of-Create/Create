package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(-16, 13);
		
		RenderSystem.translatef(0, 0, 200);
		RenderSystem.translatef(-6, 19, 0);
		RenderSystem.rotatef(-22.5f, 1, 0, 0);
		RenderSystem.rotatef(90 - 22.5f, 0, 1, 0);
		int scale = 25;

		GuiGameElement.of(shaft(Axis.X))
				.rotateBlock(-getCurrentAngle(), 0, 0)
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlocks.MECHANICAL_SAW.getDefaultState()
				.with(SawBlock.FACING, Direction.UP))
				.rotateBlock(0, 0, 0)
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE)
				.rotateBlock(0, -90, -90)
				.scale(scale)
				.render();

		RenderSystem.popMatrix();
	}

}
