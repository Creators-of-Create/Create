package com.simibubi.create.compat.jei.category.animations;

import static com.simibubi.create.foundation.utility.AnimationTickHolder.ticks;

import com.mojang.blaze3d.systems.RenderSystem;
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
		RenderSystem.pushMatrix();
		RenderSystem.enableDepthTest();
		RenderSystem.translatef(xOffset, yOffset, 0);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		RenderSystem.translatef(-45, -5, 0);
		RenderSystem.scaled(.45f, .45f, .45f);

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::shaft);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::body);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderModel(this::head);
		RenderSystem.popMatrix();
		
		if (basin) {
			RenderSystem.pushMatrix();
			ScreenElementRenderer.renderBlock(this::basin);
			RenderSystem.popMatrix();
		}

		RenderSystem.popMatrix();
	}

	private BlockState shaft() {
		float t = 25;
		RenderSystem.translatef(t, -t, -t);
		RenderSystem.rotatef(getCurrentAngle() * 2, 1, 0, 0);
		RenderSystem.translatef(-t, t, t);
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
		RenderSystem.translated(0, -verticalOffset * 50, 0);
		return AllBlockPartials.MECHANICAL_PRESS_HEAD.get();
	}
	
	private BlockState basin() {
		RenderSystem.translatef(0, 85, 0);
		return AllBlocks.BASIN.get().getDefaultState();
	}

}
