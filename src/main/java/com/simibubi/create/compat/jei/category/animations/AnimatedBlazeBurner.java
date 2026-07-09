package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import org.joml.Matrix3x2fStack;

public class AnimatedBlazeBurner extends AnimatedKinetics {

	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 23;

		float offset = (Mth.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;

		blockElement(AllBlocks.BLAZE_BURNER.getDefaultState()).atLocal(0, 1.65f, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		PartialModel blaze =
			heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE;
		PartialModel rods2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2
			: AllPartialModels.BLAZE_BURNER_RODS_2;

		blockElement(blaze).atLocal(1, 1.8f, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		blockElement(rods2).atLocal(1, 1.7f + offset, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		matrixStack.popMatrix();
	}

}
