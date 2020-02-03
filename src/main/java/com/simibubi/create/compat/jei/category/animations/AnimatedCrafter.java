package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterBlock;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class AnimatedCrafter extends AnimatedKinetics {

	boolean four;

	public AnimatedCrafter(boolean four) {
		this.four = four;
	}

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

		ScreenElementRenderer.renderBlock(() -> cogwheel(true));
		ScreenElementRenderer.renderBlock(this::body);
		GlStateManager.translatef(0, 50, 0);
		ScreenElementRenderer.renderBlock(() -> cogwheel(false));
		ScreenElementRenderer.renderBlock(this::body);

		if (four) {
			GlStateManager.translatef(50, -50, 0);
			ScreenElementRenderer.renderBlock(() -> cogwheel(false));
			ScreenElementRenderer.renderBlock(this::body);
			GlStateManager.translatef(0, 50, 0);
			ScreenElementRenderer.renderBlock(() -> cogwheel(true));
			ScreenElementRenderer.renderBlock(this::body);

		} else {
			GlStateManager.translatef(0, 50, 0);
			ScreenElementRenderer.renderBlock(() -> cogwheel(true));
			ScreenElementRenderer.renderBlock(this::body);
		}

		GlStateManager.popMatrix();
	}

	private BlockState cogwheel(boolean forward) {
		float t = 25;
		GlStateManager.translatef(t, -t, -t);
		GlStateManager.rotated(getCurrentAngle() * 2 * (forward ? 1 : -1), 0, 0, 1);
		GlStateManager.translatef(-t, t, t);
		return AllBlocks.SHAFTLESS_COGWHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.X);
	}

	private BlockState body() {
		return AllBlocks.MECHANICAL_CRAFTER.get().getDefaultState().with(MechanicalCrafterBlock.HORIZONTAL_FACING,
				Direction.WEST);
	}

}
