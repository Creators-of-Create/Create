package com.simibubi.create.content.contraptions.fluids.pipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TransparentStraightPipeRenderer extends SafeTileEntityRenderer<StraightPipeTileEntity> {

	public TransparentStraightPipeRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(StraightPipeTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		FluidPipeBehaviour pipe = TileEntityBehaviour.get(te, FluidPipeBehaviour.TYPE);
		if (pipe == null)
			return;
		FluidStack fluidStack = pipe.getFluid();
		if (fluidStack.isEmpty())
			return;

		for (Direction side : Iterate.directions) {
			if (!pipe.isConnectedTo(te.getBlockState(), side))
				continue;
			Pair<Boolean, LerpedFloat> strogestFlow = pipe.getStrogestFlow(side);
			if (strogestFlow == null)
				continue;
			LerpedFloat second = strogestFlow.getSecond();
			if (second == null)
				continue;

			float value = second.getValue(partialTicks);
			Boolean inbound = strogestFlow.getFirst();
			if (value == 1 && !inbound) {
				FluidPipeBehaviour adjacent = TileEntityBehaviour.get(te.getWorld(), te.getPos()
					.offset(side), FluidPipeBehaviour.TYPE);

				if (adjacent != null && adjacent.getFluid()
					.isEmpty())
					value -= 1e-6f;
			}

			FluidRenderer.renderFluidStream(fluidStack, side, 3 / 16f, value, inbound, buffer, ms, light);
		}

	}

}
