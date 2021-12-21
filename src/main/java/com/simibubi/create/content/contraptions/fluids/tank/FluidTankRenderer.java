package com.simibubi.create.content.contraptions.fluids.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.animation.InterpolatedChasingValue;

import com.simibubi.create.lib.util.FluidRenderUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.FluidTank;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;

public class FluidTankRenderer extends SafeTileEntityRenderer<FluidTankTileEntity> {

	public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(FluidTankTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (!te.isController())
			return;
		if (!te.window)
			return;

		InterpolatedChasingValue fluidLevel = te.getFluidLevel();
		if (fluidLevel == null)
			return;

		float capHeight = 1 / 4f;
		float tankHullWidth = 1 / 16f + 1 / 128f;
		float minPuddleHeight = 1 / 16f;
		float totalHeight = te.height - 2 * capHeight - minPuddleHeight;

		float level = fluidLevel.get(partialTicks);
		if (level < 1 / (512f * totalHeight))
			return;
		float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

		FluidTank tank = te.tankInventory;
		FluidStack fluidStack = tank.getFluid();

		if (fluidStack.isEmpty())
			return;

		boolean top = FluidRenderUtil.fillsFromTop(fluidStack.getType());

		float xMin = tankHullWidth;
		float xMax = xMin + te.width - 2 * tankHullWidth;
		float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
		float yMax = yMin + clampedLevel;

		if (top) {
			yMin += totalHeight - clampedLevel;
			yMax += totalHeight - clampedLevel;
		}

		float zMin = tankHullWidth;
		float zMax = zMin + te.width - 2 * tankHullWidth;

		ms.pushPose();
		ms.translate(0, clampedLevel - totalHeight, 0);
		FluidRenderer.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false);
		ms.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(FluidTankTileEntity te) {
		return te.isController();
	}

}
