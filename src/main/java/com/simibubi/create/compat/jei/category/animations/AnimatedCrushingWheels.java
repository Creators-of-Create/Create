package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.platform.GlStateManager;
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
		GlStateManager.enableDepthTest();
		GlStateManager.translatef(xOffset, yOffset, 0);
		GlStateManager.translatef(-45, 10, 0);
		GlStateManager.rotatef(22.5f, 0, 1, 0);
		GlStateManager.scaled(.45f, .45f, .45f);
		ScreenElementRenderer.renderBlock(this::leftWheel);
		ScreenElementRenderer.renderBlock(this::rightWheel);
	}

	private BlockState leftWheel() {
		float angle = getCurrentAngle();
		GlStateManager.translatef(-50, 0, 0);
		
		float t = 25;
		GlStateManager.translatef(t, -t, t);
		GlStateManager.rotated(angle, 0, 0, 1);
		GlStateManager.translatef(-t, t, -t);
		
		return AllBlocks.CRUSHING_WHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

	private BlockState rightWheel() {
		float angle = -getCurrentAngle();
		GlStateManager.translatef(50, 0, 0);
		
		float t = 25;
		GlStateManager.translatef(t, -t, t);
		GlStateManager.rotated(angle, 0, 0, 1);
		GlStateManager.translatef(-t, t, -t);
		
		return AllBlocks.CRUSHING_WHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

}
