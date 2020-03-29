package com.simibubi.create.modules.contraptions.components.mixer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class MechanicalMixerTileEntityRenderer extends KineticTileEntityRenderer {

	public MechanicalMixerTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		BlockState blockState = te.getBlockState();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;
		BlockPos pos = te.getPos();
		
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		
		SuperByteBuffer superBuffer = AllBlockPartials.SHAFTLESS_COGWHEEL.renderOn(blockState);
		standardKineticRotationTransform(superBuffer, te).renderInto(ms, vb);

		int packedLightmapCoords = blockState.getPackedLightmapCoords(te.getWorld(), pos);
		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI);

		SuperByteBuffer poleRender = AllBlockPartials.MECHANICAL_MIXER_POLE.renderOn(blockState);
		poleRender.translate(0, -renderedHeadOffset, 0).light(packedLightmapCoords).renderInto(ms, vb);

		SuperByteBuffer headRender = AllBlockPartials.MECHANICAL_MIXER_HEAD.renderOn(blockState);
		headRender.rotateCentered(Axis.Y, angle).translate(0, -renderedHeadOffset, 0).light(packedLightmapCoords)
				.renderInto(ms, vb);
	}

}
