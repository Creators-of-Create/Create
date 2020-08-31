package com.simibubi.create.content.contraptions.fluids.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

public class SpoutRenderer extends SafeTileEntityRenderer<SpoutTileEntity> {

	public SpoutRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	static final AllBlockPartials[] BITS =
		{ AllBlockPartials.SPOUT_TOP, AllBlockPartials.SPOUT_MIDDLE, AllBlockPartials.SPOUT_BOTTOM };

	@Override
	protected void renderSafe(SpoutTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		Pair<FluidStack, LerpedFloat> fluid = te.getFluid();
		FluidStack fluidStack = fluid.getFirst();
		float level = fluid.getSecond()
			.getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			float min = 2.5f / 16f;
			float max = min + (11 / 16f);
			float yOffset = (11 / 16f) * level;
			ms.push();
			ms.translate(0, yOffset, 0);
			FluidRenderer.renderTiledFluidBB(fluidStack, min, min - yOffset, min, max, min, max, buffer, ms, light,
				false);
			ms.pop();
		}

		int processingTicks = te.processingTicks;
		float processingPT = te.processingTicks - partialTicks;

		float radius = 0;
		if (processingTicks != -1) {
			float processingProgress = 1 - (processingPT - 5) / 10;
			processingProgress = MathHelper.clamp(processingProgress, 0, 1);
			radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1) / 32f;
			AxisAlignedBB bb = new AxisAlignedBB(0.5, .5, 0.5, 0.5, -1.2, 0.5).grow(radius);
			FluidRenderer.renderTiledFluidBB(fluidStack, (float) bb.minX, (float) bb.minY, (float) bb.minZ,
				(float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, buffer, ms, light, true);

		}

		ms.push();
		for (AllBlockPartials bit : BITS) {
			bit.renderOn(te.getBlockState())
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
			ms.translate(0, -3 * radius, 0);
		}
		ms.pop();

	}

}
