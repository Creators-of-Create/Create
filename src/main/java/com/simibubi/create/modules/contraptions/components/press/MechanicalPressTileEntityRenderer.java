package com.simibubi.create.modules.contraptions.components.press;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;

public class MechanicalPressTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		BlockState state = getRenderedHeadBlockState(te);
		BlockPos pos = te.getPos();
		int packedLightmapCoords = state.getPackedLightmapCoords(getWorld(), pos);
		float renderedHeadOffset = ((MechanicalPressTileEntity) te).getRenderedHeadOffset(partialTicks);

		SuperByteBuffer headRender = CreateClient.bufferCache.renderGenericBlockModel(state);
		headRender.translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords).renderInto(buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(HORIZONTAL_FACING).getAxis());
	}

	protected BlockState getRenderedHeadBlockState(KineticTileEntity te) {
		return AllBlocks.MECHANICAL_PRESS_HEAD.get().getDefaultState().with(HORIZONTAL_FACING,
				te.getBlockState().get(HORIZONTAL_FACING));
	}

}
