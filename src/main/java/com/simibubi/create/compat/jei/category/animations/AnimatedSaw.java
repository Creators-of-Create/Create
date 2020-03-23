package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 50;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.enableDepthTest();
		RenderSystem.translatef(xOffset, yOffset, 0);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		RenderSystem.translatef(-45, -5, 0);
		RenderSystem.scaled(.6f, .6f, .6f);

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::shaft);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::block);
		RenderSystem.popMatrix();

		RenderSystem.popMatrix();
	}

	private BlockState shaft() {
		float t = 25;
		RenderSystem.translatef(t, -t, t);
		RenderSystem.rotated(-getCurrentAngle() * 2, 0, 0, 1);
		RenderSystem.translatef(-t, t, -t);
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

	private BlockState block() {
		return AllBlocks.SAW.get().getDefaultState().with(BlockStateProperties.FACING, Direction.UP)
				.with(SawBlock.RUNNING, true).with(SawBlock.AXIS_ALONG_FIRST_COORDINATE, true);
	}

}
