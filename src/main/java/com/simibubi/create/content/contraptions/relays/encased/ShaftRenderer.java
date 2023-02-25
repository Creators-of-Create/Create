package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ShaftRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {

	public ShaftRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
