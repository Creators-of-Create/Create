package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 150;
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
		ScreenElementRenderer.renderModel(this::cogwheel);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::body);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderModel(this::pole);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderModel(this::head);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::basin);
		RenderSystem.popMatrix();

		RenderSystem.popMatrix();
	}

	private IBakedModel cogwheel() {
		float t = 25;
		RenderSystem.translatef(t, -t, -t);
		RenderSystem.rotatef(getCurrentAngle() * 2, 0, 1, 0);
		RenderSystem.translatef(-t, t, t);
		return AllBlockPartials.SHAFTLESS_COGWHEEL.get();
	}

	private BlockState body() {
		return AllBlocks.MECHANICAL_MIXER.get().getDefaultState();
	}

	private IBakedModel pole() {
		RenderSystem.translatef(0, 51, 0);
		return AllBlockPartials.MECHANICAL_MIXER_POLE.get();
	}

	private IBakedModel head() {
		float t = 25;
		RenderSystem.translatef(0, 51, 0);
		RenderSystem.translatef(t, -t, -t);
		RenderSystem.rotatef(getCurrentAngle() * 4, 0, 1, 0);
		RenderSystem.translatef(-t, t, t);
		return AllBlockPartials.MECHANICAL_MIXER_HEAD.get();
	}

	private BlockState basin() {
		RenderSystem.translatef(0, 85, 0);
		return AllBlocks.BASIN.get().getDefaultState();
	}
}
