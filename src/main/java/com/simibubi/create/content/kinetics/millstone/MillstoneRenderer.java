package com.simibubi.create.content.kinetics.millstone;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class MillstoneRenderer extends KineticBlockEntityRenderer<MillstoneBlockEntity> {

	public MillstoneRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(MillstoneBlockEntity be, BlockState state) {
		return CachedBuffers.partial(AllPartialModels.MILLSTONE_COG, state);
	}

}
