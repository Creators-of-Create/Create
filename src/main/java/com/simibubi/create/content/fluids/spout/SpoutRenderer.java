package com.simibubi.create.content.fluids.spout;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;

public class SpoutRenderer extends SafeBlockEntityRenderer<SpoutBlockEntity> {

	public SpoutRenderer(BlockEntityRendererProvider.Context context) {
	}

	static final PartialModel[] BITS =
		{ AllPartialModels.SPOUT_TOP, AllPartialModels.SPOUT_MIDDLE, AllPartialModels.SPOUT_BOTTOM };

	@Override
	protected void renderSafe(SpoutBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		SmartFluidTankBehaviour tank = be.tank;
		if (tank == null)
			return;

		TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack fluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel()
			.getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			boolean top = fluidStack.getFluid()
					.getAttributes()
					.isLighterThanAir();

			level = Math.max(level, 0.175f);
			float min = 2.5f / 16f;
			float max = min + (11 / 16f);
			float yOffset = (11 / 16f) * level;

			ms.pushPose();
			if (!top) ms.translate(0, yOffset, 0);
			else ms.translate(0, max - min, 0);

			FluidRenderer.renderFluidBox(fluidStack,
					min, min - yOffset, min,
					max, min, max,
					buffer, ms, light, false);

			ms.popPose();
		}

		int processingTicks = be.processingTicks;
		float processingPT = processingTicks - partialTicks;
		float processingProgress = 1 - (processingPT - 5) / 10;
		processingProgress = Mth.clamp(processingProgress, 0, 1);
		float radius = 0;

		if (processingTicks != -1) {
			radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
			AABB bb = new AABB(0.5, .5, 0.5, 0.5, -1.2, 0.5).inflate(radius / 32f);
			FluidRenderer.renderFluidBox(fluidStack, (float) bb.minX, (float) bb.minY, (float) bb.minZ,
				(float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, buffer, ms, light, true);
		}

		float squeeze = radius;
		if (processingPT < 0)
			squeeze = 0;
		else if (processingPT < 2)
			squeeze = Mth.lerp(processingPT / 2f, 0, -1);
		else if (processingPT < 10)
			squeeze = -1;

		ms.pushPose();
		for (PartialModel bit : BITS) {
			CachedBufferer.partial(bit, be.getBlockState())
					.light(light)
					.renderInto(ms, buffer.getBuffer(RenderType.solid()));
			ms.translate(0, -3 * squeeze / 32f, 0);
		}
		ms.popPose();

	}

}
