package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.ColoredOverlayBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BrassDiodeRenderer extends ColoredOverlayBlockEntityRenderer<BrassDiodeBlockEntity> {

	public BrassDiodeRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected int getColor(BrassDiodeBlockEntity be, float partialTicks) {
		return Color.mixColors(0x2C0300, 0xCD0000, be.getProgress());
	}

	@Override
	protected SuperByteBuffer getOverlayBuffer(BrassDiodeBlockEntity be) {
		return CachedBuffers.partial(AllPartialModels.FLEXPEATER_INDICATOR, be.getBlockState());
	}

}
