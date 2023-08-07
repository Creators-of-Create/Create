package com.simibubi.create.content.contraptions.fluids.tank;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankRenderer extends SafeTileEntityRenderer<FluidTankTileEntity> {

	public FluidTankRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(FluidTankTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (!te.isController())
			return;
		if (!te.window) {
			if (te.boiler.isActive())
				renderAsBoiler(te, partialTicks, ms, buffer, light, overlay);
			return;
		}

		LerpedFloat fluidLevel = te.getFluidLevel();
		if (fluidLevel == null)
			return;

		float capHeight = 1 / 4f;
		float tankHullWidth = 1 / 16f + 1 / 128f;
		float minPuddleHeight = 1 / 16f;
		float totalHeight = te.height - 2 * capHeight - minPuddleHeight;

		float level = fluidLevel.getValue(partialTicks);
		if (level < 1 / (512f * totalHeight))
			return;
		float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

		FluidTank tank = te.tankInventory;
		FluidStack fluidStack = tank.getFluid();

		if (fluidStack.isEmpty())
			return;

		boolean top = fluidStack.getFluid()
			.getAttributes()
			.isLighterThanAir();

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
		FluidRenderer.renderFluidBox(fluidStack.getFluid(), fluidStack.getAmount(), xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false);
		ms.popPose();
	}

	protected void renderAsBoiler(FluidTankTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		ms.pushPose();
		TransformStack msr = TransformStack.cast(ms);
		msr.translate(te.width / 2f, 0.5, te.width / 2f);

		float dialPivot = 5.75f / 16;
		float progress = te.boiler.gauge.getValue(partialTicks);

		for (Direction d : Iterate.horizontalDirections) {
			ms.pushPose();
			CachedPartialBuffers.partial(AllBlockPartials.BOILER_GAUGE, blockState)
				.rotate(Direction.Axis.Y, Mth.DEG_TO_RAD * d.toYRot())
				.translate(-.5, -.5, -.5)
				.translate(te.width / 2f - 6 / 16f, 0, 0)
				.light(light)
				.renderInto(ms, vb);
			CachedPartialBuffers.partial(AllBlockPartials.BOILER_GAUGE_DIAL, blockState)
				.rotate(Direction.Axis.Y, Mth.DEG_TO_RAD * d.toYRot())
				.translate(-.5, -.5, -.5)
				.translate(te.width / 2f - 6 / 16f, 0, 0)
				.translate(0, dialPivot, dialPivot)
				.rotate(Direction.Axis.X, Mth.DEG_TO_RAD * -90 * progress)
				.translate(0, -dialPivot, -dialPivot)
				.light(light)
				.renderInto(ms, vb);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(FluidTankTileEntity te) {
		return te.isController();
	}

}
