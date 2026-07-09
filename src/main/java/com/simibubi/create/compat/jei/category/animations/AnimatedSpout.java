package com.simibubi.create.compat.jei.category.animations;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3x2fStack;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 20;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(graphics, 0, 0, 0);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.pushMatrix();

		blockElement(AllPartialModels.SPOUT_TOP)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		matrixStack.translate(0, -3 * squeeze / 32f);
		blockElement(AllPartialModels.SPOUT_MIDDLE)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		matrixStack.translate(0, -3 * squeeze / 32f);
		blockElement(AllPartialModels.SPOUT_BOTTOM)
			.scale(scale)
			.render(graphics, 0, 0, 0);
		matrixStack.translate(0, -3 * squeeze / 32f);

		matrixStack.popMatrix();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}

}
