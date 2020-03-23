package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	@Override
	public int getWidth() {
		return 150;
	}

	@Override
	public int getHeight() {
		return 100;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		RenderSystem.translatef(xOffset, yOffset, 0);
		RenderSystem.translatef(-45, 10, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		RenderSystem.scaled(.45f, .45f, .45f);
		ScreenElementRenderer.renderBlock(this::leftWheel);
		ScreenElementRenderer.renderBlock(this::rightWheel);
	}

	private BlockState leftWheel() {
		float angle = getCurrentAngle();
		RenderSystem.translatef(-50, 0, 0);
		
		float t = 25;
		RenderSystem.translatef(t, -t, t);
		RenderSystem.rotated(angle, 0, 0, 1);
		RenderSystem.translatef(-t, t, -t);
		
		return AllBlocks.CRUSHING_WHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

	private BlockState rightWheel() {
		float angle = -getCurrentAngle();
		RenderSystem.translatef(50, 0, 0);
		
		float t = 25;
		RenderSystem.translatef(t, -t, t);
		RenderSystem.rotated(angle, 0, 0, 1);
		RenderSystem.translatef(-t, t, -t);
		
		return AllBlocks.CRUSHING_WHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

}
