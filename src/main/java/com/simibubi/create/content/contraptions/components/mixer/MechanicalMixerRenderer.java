package com.simibubi.create.content.contraptions.components.mixer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class MechanicalMixerRenderer extends KineticTileEntityRenderer {

	public MechanicalMixerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;
		BlockPos pos = te.getPos();

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		int packedLightmapCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), blockState, pos);
		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI);

		SuperByteBuffer poleRender = AllBlockPartials.MECHANICAL_MIXER_POLE.renderOn(blockState);
		poleRender.translate(0, -renderedHeadOffset, 0)
			.light(packedLightmapCoords)
			.renderInto(ms, vb);

		SuperByteBuffer headRender = AllBlockPartials.MECHANICAL_MIXER_HEAD.renderOn(blockState);
		headRender.rotateCentered(Direction.UP, angle)
			.translate(0, -renderedHeadOffset, 0)
			.light(packedLightmapCoords)
			.renderInto(ms, vb);
	}

	@Override
	protected InstanceBuffer<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return AllBlockPartials.SHAFTLESS_COGWHEEL.renderOnRotating(ctx, ctx.te.getBlockState());
	}
}
