package com.simibubi.create.content.contraptions.relays.gauge;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock.Type;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class GaugeRenderer extends KineticTileEntityRenderer {

	protected GaugeBlock.Type type;

	public static GaugeRenderer speed(BlockEntityRenderDispatcher dispatcher) {
		return new GaugeRenderer(dispatcher, Type.SPEED);
	}

	public static GaugeRenderer stress(BlockEntityRenderDispatcher dispatcher) {
		return new GaugeRenderer(dispatcher, Type.STRESS);
	}

	protected GaugeRenderer(BlockEntityRenderDispatcher dispatcher, GaugeBlock.Type type) {
		super(dispatcher);
		this.type = type;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		BlockState gaugeState = te.getBlockState();
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;

		PartialModel partialModel = (type == Type.SPEED ? AllBlockPartials.GAUGE_HEAD_SPEED : AllBlockPartials.GAUGE_HEAD_STRESS);
		SuperByteBuffer headBuffer =
				PartialBufferer.get(partialModel, gaugeState);
		SuperByteBuffer dialBuffer = PartialBufferer.get(AllBlockPartials.GAUGE_DIAL, gaugeState);

		float dialPivot = 5.75f / 16;
		float progress = Mth.lerp(partialTicks, gaugeTE.prevDialState, gaugeTE.dialState);

		for (Direction facing : Iterate.directions) {
			if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(te.getLevel(), te.getBlockPos(), gaugeState,
					facing))
				continue;

			VertexConsumer vb = buffer.getBuffer(RenderType.solid());
			rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot)
				.rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
				.translate(0, -dialPivot, -dialPivot)
				.light(light)
				.renderInto(ms, vb);
			rotateBufferTowards(headBuffer, facing).light(light)
				.renderInto(ms, vb);
		}
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Direction.UP, (float) ((-target.toYRot() - 90) / 180 * Math.PI));
	}

}
