package com.simibubi.create.modules.contraptions.components.mixer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class MechanicalMixerTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState blockState = te.getBlockState();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;
		BlockPos pos = te.getPos();
		
		SuperByteBuffer superBuffer = AllBlockPartials.SHAFTLESS_COGWHEEL.renderOn(blockState);
		standardKineticRotationTransform(superBuffer, te, getWorld()).translate(x, y, z).renderInto(buffer);

		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI);

		SuperByteBuffer poleRender = AllBlockPartials.MECHANICAL_MIXER_POLE.renderOn(blockState);
		poleRender.translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords).renderInto(buffer);

		SuperByteBuffer headRender = AllBlockPartials.MECHANICAL_MIXER_HEAD.renderOn(blockState);
		headRender.rotateCentered(Axis.Y, angle).translate(x, y - renderedHeadOffset, z).light(packedLightmapCoords)
				.renderInto(buffer);
	}

}
