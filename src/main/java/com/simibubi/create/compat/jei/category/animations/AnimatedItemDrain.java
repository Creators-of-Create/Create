package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedItemDrain extends AnimatedKinetics {

	private FluidStack fluid;

	public AnimatedItemDrain withFluid(FluidStack fluid) {
		this.fluid = fluid;
		return this;
	}

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 20;

		GuiGameElement.of(AllBlocks.ITEM_DRAIN.getDefaultState())
			.scale(scale)
			.render(matrixStack);

		Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance()
			.getBuffer());
		MatrixStack ms = new MatrixStack();
		ms.scale(scale, -scale, scale);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderTiledFluidBB(fluid, from, from, from, to, 3/4f, to, buffer, ms, 0xf000f0, false);
		buffer.draw();

		matrixStack.pop();
	}
}
