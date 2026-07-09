package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlocks;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3x2fStack;

public class AnimatedItemDrain extends AnimatedKinetics {

	private FluidStack fluid;

	public AnimatedItemDrain withFluid(FluidStack fluid) {
		this.fluid = fluid;
		return this;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);
		int scale = 20;

		blockElement(AllBlocks.ITEM_DRAIN.getDefaultState())
			.scale(scale)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}
}
