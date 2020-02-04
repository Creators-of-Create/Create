package com.simibubi.create.modules.contraptions.components.press;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;

public class MechanicalPressTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);

		BlockPos pos = te.getPos();
		BlockState blockState = te.getBlockState();
		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		float renderedHeadOffset = ((MechanicalPressTileEntity) te).getRenderedHeadOffset(partialTicks);

		SuperByteBuffer headRender = AllBlockPartials.MECHANICAL_PRESS_HEAD.renderOnHorizontal(blockState);
		headRender.translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords).renderInto(buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(HORIZONTAL_FACING).getAxis());
	}

}
