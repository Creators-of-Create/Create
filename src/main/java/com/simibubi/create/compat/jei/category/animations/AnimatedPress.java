package com.simibubi.create.compat.jei.category.animations;

import static com.simibubi.create.foundation.utility.AnimationTickHolder.ticks;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AnimatedPress extends AnimatedKinetics {

	private boolean basin;
	
	public AnimatedPress(boolean basin) {
		this.basin = basin;
	}
	
	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 100;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		GlStateManager.pushMatrix();
		GlStateManager.enableDepthTest();
		GlStateManager.translatef(xOffset, yOffset, 0);
		GlStateManager.rotatef(-15.5f, 1, 0, 0);
		GlStateManager.rotatef(22.5f, 0, 1, 0);
		GlStateManager.translatef(-45, -5, 0);
		GlStateManager.scaled(.45f, .45f, .45f);

		GlStateManager.pushMatrix();
		ScreenElementRenderer.renderBlock(this::shaft);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		ScreenElementRenderer.renderBlock(this::body);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		ScreenElementRenderer.renderModel(this::head);
		GlStateManager.popMatrix();
		
		if (basin) {
			GlStateManager.pushMatrix();
			ScreenElementRenderer.renderBlock(this::basin);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	private BlockState shaft() {
		float t = 25;
		GlStateManager.translatef(t, -t, -t);
		GlStateManager.rotated(getCurrentAngle() * 2, 1, 0, 0);
		GlStateManager.translatef(-t, t, t);
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.Z);
	}

	private BlockState body() {
		return AllBlocks.MECHANICAL_PRESS.get().getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING,
				Direction.SOUTH);
	}

	private IBakedModel head() {
		float cycle = (ticks + Minecraft.getInstance().getRenderPartialTicks()) % 30;
		float verticalOffset = 0;
		if (cycle < 10) {
			float progress = cycle / 10;
			verticalOffset = -(progress * progress * progress);
		} else if (cycle < 15) {
			verticalOffset = -1;
		} else if (cycle < 20) {
			verticalOffset = -1 + (1 - ((20 - cycle) / 5));
		} else {
			verticalOffset = 0;
		}
		GlStateManager.translated(0, -verticalOffset * 50, 0);
		return AllBlockPartials.MECHANICAL_PRESS_HEAD.get();
	}
	
	private BlockState basin() {
		GlStateManager.translatef(0, 85, 0);
		return AllBlocks.BASIN.get().getDefaultState();
	}

}
