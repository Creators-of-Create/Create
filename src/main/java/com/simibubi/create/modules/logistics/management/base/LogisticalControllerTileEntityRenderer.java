package com.simibubi.create.modules.logistics.management.base;

import static com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.TYPE;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.ColoredIndicatorRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public class LogisticalControllerTileEntityRenderer extends TileEntityRendererFast<LogisticalActorTileEntity> {

	@Override
	public void renderTileEntityFast(LogisticalActorTileEntity te, double x, double y, double z,
			float partialTicks, int destroyStage, BufferBuilder buffer) {
		BlockPos pos = te.getPos();
		BlockState blockState = te.getBlockState();
		BlockState renderedState = AllBlocks.LOGISTICAL_CONTROLLER_INDICATOR.get().getDefaultState()
				.with(FACING, blockState.get(FACING)).with(TYPE, blockState.get(TYPE));
		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		buffer.putBulkData(ColoredIndicatorRenderer.get(renderedState).getTransformed((float) x, (float) y, (float) z,
				te.getColor(), packedLightmapCoords));
	}

}
