package com.simibubi.create.compat.jei.category.animations;

import static com.simibubi.create.foundation.utility.AnimationTickHolder.ticks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction.Axis;

public class AnimatedPress extends AnimatedKinetics {

	private boolean basin;

	public AnimatedPress(boolean basin) {
		this.basin = basin;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 100);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = basin ? 20 : 24;

		GuiGameElement.of(shaft(Axis.Z))
				.rotateBlock(0, 0, getCurrentAngle())
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlocks.MECHANICAL_PRESS.getDefault())
				.scale(scale)
				.render();

		GuiGameElement.of(AllBlockPartials.MECHANICAL_PRESS_HEAD)
				.atLocal(0, -getAnimatedHeadOffset(), 0)
				.scale(scale)
				.render();

		if (basin)
			GuiGameElement.of(AllBlocks.BASIN.getDefault())
					.atLocal(0, 1.65, 0)
					.scale(scale)
					.render();

		RenderSystem.popMatrix();
	}

	private float getAnimatedHeadOffset() {
		float cycle = (ticks + Minecraft.getInstance()
				.getRenderPartialTicks()) % 30;
		if (cycle < 10) {
			float progress = cycle / 10;
			return -(progress * progress * progress);
		}
		if (cycle < 15)
			return -1;
		if (cycle < 20)
			return -1 + (1 - ((20 - cycle) / 5));
		return 0;
	}

}
