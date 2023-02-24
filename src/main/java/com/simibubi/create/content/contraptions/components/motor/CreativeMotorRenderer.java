package com.simibubi.create.content.contraptions.components.motor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeMotorRenderer extends KineticBlockEntityRenderer<CreativeMotorBlockEntity> {

	public CreativeMotorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(CreativeMotorBlockEntity be, BlockState state) {
		return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}

}
