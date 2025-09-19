package com.simibubi.create.compat.jei.category.animations;

import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;

import net.minecraftforge.fluids.FluidStack;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);
		matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
		int scale = 20;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(graphics);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.pushPose();

		blockElement(AllPartialModels.SPOUT_TOP)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllPartialModels.SPOUT_MIDDLE)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllPartialModels.SPOUT_BOTTOM)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);

		matrixStack.popPose();

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(graphics);

		AnimatedKinetics.DEFAULT_LIGHTING.applyLighting();
		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance()
			.getBuilder());
		matrixStack.pushPose();
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);
		float from = 3f / 16f;
		float to = 17f / 16f;
		FluidStack fluidStack = fluids.get(0);
		ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, from, from, from, to, to, to, graphics.bufferSource(), matrixStack, LightTexture.FULL_BRIGHT, false, true);
		matrixStack.popPose();

		float width = 1 / 128f * squeeze;
		matrixStack.translate(scale / 2f, scale * 1.5f, scale / 2f);
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);
		matrixStack.translate(-0.5f, 0, -0.5f);
		from = -width / 2 + 0.5f;
		to = width / 2 + 0.5f;
		ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, from, 0, from, to, 2, to, graphics.bufferSource(), matrixStack, LightTexture.FULL_BRIGHT, false, true);
		buffer.endBatch();
		Lighting.setupFor3DItems();

		matrixStack.popPose();
	}

}
