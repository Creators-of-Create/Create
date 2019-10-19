package com.simibubi.create.modules.contraptions.receivers;

import java.nio.ByteBuffer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BufferManipulator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;

public class MechanicalPressTileEntityRenderer extends KineticTileEntityRenderer {

	protected class HeadTranslator extends BufferManipulator {

		public HeadTranslator(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(float xIn, float yIn, float zIn, float pushDistance, int packedLightCoords) {
			original.rewind();
			mutable.rewind();

			for (int vertex = 0; vertex < vertexCount(original); vertex++) {
				putPos(mutable, vertex, getX(original, vertex) + xIn, getY(original, vertex) + yIn - pushDistance,
						getZ(original, vertex) + zIn);
				putLight(mutable, vertex, packedLightCoords);
			}

			return mutable;
		}
	}

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		final BlockState state = getRenderedHeadBlockState(te);
		cacheIfMissing(state, getWorld(), HeadTranslator::new);

		final BlockPos pos = te.getPos();

		int packedLightmapCoords = state.getPackedLightmapCoords(getWorld(), pos);
		buffer.putBulkData(((HeadTranslator) cachedBuffers.get(state)).getTransformed((float) x, (float) y, (float) z,
				((MechanicalPressTileEntity) te).getRenderedHeadOffset(partialTicks), packedLightmapCoords));
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getAxis());
	}

	protected BlockState getRenderedHeadBlockState(KineticTileEntity te) {
		return AllBlocks.MECHANICAL_PRESS_HEAD.get().getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING,
				te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

}
