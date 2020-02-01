package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class MechanicalBearingTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		MechanicalBearingTileEntity bearingTe = (MechanicalBearingTileEntity) te;
		final Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
		BlockState capState = AllBlocks.MECHANICAL_BEARING_TOP.get().getDefaultState().with(BlockStateProperties.FACING,
				facing);

		SuperByteBuffer superBuffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE, capState);
		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks);
		kineticRotationTransform(superBuffer, bearingTe, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), getWorld());
		superBuffer.translate(x, y, z).renderInto(buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT_HALF.get().getDefaultState().with(BlockStateProperties.FACING,
				te.getBlockState().get(BlockStateProperties.FACING).getOpposite());
	}

}
