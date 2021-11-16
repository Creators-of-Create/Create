package com.simibubi.create.compat.jei.category.animations;

import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(matrixStack);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.pushPose();

		blockElement(AllBlockPartials.SPOUT_TOP)
			.scale(scale)
			.render(matrixStack);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllBlockPartials.SPOUT_MIDDLE)
			.scale(scale)
			.render(matrixStack);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllBlockPartials.SPOUT_BOTTOM)
			.scale(scale)
			.render(matrixStack);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);

		matrixStack.popPose();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrixStack);

		AnimatedKinetics.DEFAULT_LIGHTING.applyLighting();
		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance()
			.getBuilder());
		matrixStack.pushPose();
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderTiledFluidBB(fluids.get(0), from, from, from, to, to, to, buffer, matrixStack, LightTexture.FULL_BRIGHT, false);
		matrixStack.popPose();

		float width = 1 / 128f * squeeze;
		matrixStack.translate(scale / 2f, scale * 1.5f, scale / 2f);
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);
		matrixStack.translate(-width / 2, 0, -width / 2);
		FluidRenderer.renderTiledFluidBB(fluids.get(0), 0, -0.001f, 0, width, 2.001f, width, buffer, matrixStack, LightTexture.FULL_BRIGHT,
			false);
		buffer.endBatch();
		Lighting.setupFor3DItems();

		matrixStack.popPose();
	}

}
