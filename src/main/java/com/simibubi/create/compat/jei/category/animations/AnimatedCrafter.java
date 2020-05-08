package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;

public class AnimatedCrafter extends AnimatedKinetics {

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
		GlStateManager.pushMatrix();
		GlStateManager.enableDepthTest();
		GlStateManager.rotatef(-15.5f, 1, 0, 0);
		GlStateManager.rotatef(-22.5f, 0, 1, 0);
		GlStateManager.translatef(xOffset, yOffset, 0);
		GlStateManager.translatef(-45, -5, 0);
		GlStateManager.scaled(.45f, .45f, .45f);

		ScreenElementRenderer.renderModel(() -> cogwheel(true));
		ScreenElementRenderer.renderBlock(this::body);
		GlStateManager.translatef(50, 0, 0);

		GlStateManager.popMatrix();
	}

	private IBakedModel cogwheel(boolean forward) {
		float t = 25;
		GlStateManager.translatef(t, -t, -t);
		GlStateManager.rotated(getCurrentAngle() * 2 * (forward ? 1 : -1), 0, 0, 1);
		GlStateManager.rotated(90, 1, 0, 0);
		GlStateManager.translatef(-t, t, t);
		return AllBlockPartials.SHAFTLESS_COGWHEEL.get();
	}

	private BlockState body() {
		return AllBlocks.MECHANICAL_CRAFTER
				.get()
				.getDefaultState()
				.with(MechanicalCrafterBlock.HORIZONTAL_FACING, Direction.WEST);
	}

}
