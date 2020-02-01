package com.simibubi.create.modules.contraptions.components.mixer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

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

		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;
		BlockState poleState = AllBlocks.MECHANICAL_MIXER_POLE.get().getDefaultState();
		BlockState headState = AllBlocks.MECHANICAL_MIXER_HEAD.get().getDefaultState();
		BlockPos pos = te.getPos();

		int packedLightmapCoords = poleState.getPackedLightmapCoords(getWorld(), pos);
		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI);

		SuperByteBuffer poleRender = CreateClient.bufferCache.renderGenericBlockModel(poleState);
		poleRender.translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords).renderInto(buffer);

		SuperByteBuffer headRender = CreateClient.bufferCache.renderGenericBlockModel(headState);
		headRender.rotateCentered(Axis.Y, angle).translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords)
				.renderInto(buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFTLESS_COGWHEEL.get().getDefaultState().with(BlockStateProperties.AXIS, Axis.Y);
	}

}
