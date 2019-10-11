package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.ColoredIndicatorRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public class FlexpeaterTileEntityRenderer extends TileEntityRendererFast<FlexpeaterTileEntity> {

	@Override
	public void renderTileEntityFast(FlexpeaterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockPos pos = te.getPos();

		BlockState renderedState = AllBlocks.FLEXPEATER_INDICATOR.get().getDefaultState();
		BlockState blockState = te.getBlockState();
		int color = ColorHelper.mixColors(0x2C0300, 0xCD0000, te.state / (float) te.maxState);
		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);

		buffer.putBulkData(ColoredIndicatorRenderer.get(renderedState).getTransformed((float) x, (float) y, (float) z,
				color, packedLightmapCoords));
	}

}
