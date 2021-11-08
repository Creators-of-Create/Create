package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.ColoredOverlayTileEntityRenderer;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BrassDiodeRenderer extends ColoredOverlayTileEntityRenderer<BrassDiodeTileEntity> {

	public BrassDiodeRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected int getColor(BrassDiodeTileEntity te, float partialTicks) {
		return Color.mixColors(0x2C0300, 0xCD0000, te.getProgress());
	}

	@Override
	protected SuperByteBuffer getOverlayBuffer(BrassDiodeTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.FLEXPEATER_INDICATOR, te.getBlockState());
	}

}
