package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		RenderSystem.translatef(xOffset, yOffset, 100);
		RenderSystem.rotatef(-22.5f, 0, 1, 0);
		int scale = 22;
		
		BlockState wheel = AllBlocks.CRUSHING_WHEEL.get()
				.getDefaultState()
				.with(BlockStateProperties.AXIS, Axis.X);

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render();

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render();
	}

}
