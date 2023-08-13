package com.simibubi.create.content.fluids.pump;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class PumpRenderer extends KineticBlockEntityRenderer<PumpBlockEntity> {

	public PumpRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(PumpBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.MECHANICAL_PUMP_COG, state);
	}

}
