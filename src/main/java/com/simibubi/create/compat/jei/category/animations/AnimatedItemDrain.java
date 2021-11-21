package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.UIRenderHelper;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedItemDrain extends AnimatedKinetics {

	private FluidStack fluid;

	public AnimatedItemDrain withFluid(FluidStack fluid) {
		this.fluid = fluid;
		return this;
	}

	@Override
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(AllBlocks.ITEM_DRAIN.getDefaultState())
			.scale(scale)
			.render(matrixStack);

		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance()
			.getBuilder());
		PoseStack ms = new PoseStack();
		UIRenderHelper.flipForGuiRender(ms);
		ms.scale(scale, scale, scale);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderFluidBox(fluid, from, from, from, to, 3/4f, to, buffer, ms, LightTexture.FULL_BRIGHT, false);
		buffer.endBatch();

		matrixStack.popPose();
	}
}
