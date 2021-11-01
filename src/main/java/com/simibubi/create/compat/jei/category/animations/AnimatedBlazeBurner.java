package com.simibubi.create.compat.jei.category.animations;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;

public class AnimatedBlazeBurner extends AnimatedKinetics {

	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 200);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 23;

		blockElement(AllBlocks.BLAZE_BURNER.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		float offset = (Mth.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;
		PartialModel blaze = AllBlockPartials.BLAZES.get(heatLevel);
		blockElement(blaze)
			.atLocal(1, 1.65 + offset, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
