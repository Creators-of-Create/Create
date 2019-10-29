package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.MechanicalPressTileEntityRenderer.HeadTranslator;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class MechanicalMixerTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		final BlockState poleState = AllBlocks.MECHANICAL_MIXER_POLE.get().getDefaultState();
		final BlockState headState = AllBlocks.MECHANICAL_MIXER_HEAD.get().getDefaultState();
		cacheIfMissing(poleState, HeadTranslator::new);
		cacheIfMissing(headState, HeadTranslator::new);
		final BlockPos pos = te.getPos();

		int packedLightmapCoords = poleState.getPackedLightmapCoords(getWorld(), pos);
		float speed = ((MechanicalMixerTileEntity) te).getRenderedHeadRotationSpeed(partialTicks);
		float renderedHeadOffset = ((MechanicalMixerTileEntity) te).getRenderedHeadOffset(partialTicks) + 7 / 16f;
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 2) % 360) / 180 * (float) Math.PI);

		buffer.putBulkData(((HeadTranslator) cachedBuffers.get(poleState)).getTransformed((float) x, (float) y,
				(float) z, renderedHeadOffset, packedLightmapCoords));
		buffer.putBulkData(((HeadTranslator) cachedBuffers.get(headState)).getTransformedRotated((float) x, (float) y,
				(float) z, renderedHeadOffset, angle, packedLightmapCoords));
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFTLESS_COGWHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.Y);
	}

}
