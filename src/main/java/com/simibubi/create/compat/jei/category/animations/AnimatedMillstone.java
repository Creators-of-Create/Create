package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;

public class AnimatedMillstone extends AnimatedKinetics {

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
		RenderSystem.scaled(.45f, .45f, .45f);

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderModel(this::cogwheel);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		ScreenElementRenderer.renderBlock(this::body);
		RenderSystem.popMatrix();

		RenderSystem.popMatrix();
	}

	private IBakedModel cogwheel() {
		float t = 25;
		RenderSystem.translatef(t, -t, -t);
		RenderSystem.rotatef(getCurrentAngle() * 2, 0, 1, 0);
		RenderSystem.translatef(-t, t, t);
		return AllBlockPartials.MILLSTONE_COG.get();
	}

	private BlockState body() {
		return AllBlocks.MILLSTONE.get().getDefaultState();
	}
}
