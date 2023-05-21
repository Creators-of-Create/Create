package com.simibubi.create.content.kinetics.gauge;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.gauge.GaugeBlock.Type;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class GaugeRenderer extends ShaftRenderer<GaugeBlockEntity> {

	protected GaugeBlock.Type type;

	public static GaugeRenderer speed(BlockEntityRendererProvider.Context context) {
		return new GaugeRenderer(context, Type.SPEED);
	}

	public static GaugeRenderer stress(BlockEntityRendererProvider.Context context) {
		return new GaugeRenderer(context, Type.STRESS);
	}

	protected GaugeRenderer(BlockEntityRendererProvider.Context context, GaugeBlock.Type type) {
		super(context);
		this.type = type;
	}

	@Override
	protected void renderSafe(GaugeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (Backend.canUseInstancing(be.getLevel())) return;

		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		BlockState gaugeState = be.getBlockState();
		GaugeBlockEntity gaugeBE = (GaugeBlockEntity) be;

		PartialModel partialModel = (type == Type.SPEED ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS);
		SuperByteBuffer headBuffer =
				CachedBufferer.partial(partialModel, gaugeState);
		SuperByteBuffer dialBuffer = CachedBufferer.partial(AllPartialModels.GAUGE_DIAL, gaugeState);

		float dialPivot = 5.75f / 16;
		float progress = Mth.lerp(partialTicks, gaugeBE.prevDialState, gaugeBE.dialState);

		for (Direction facing : Iterate.directions) {
			if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(be.getLevel(), be.getBlockPos(), gaugeState,
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

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Direction.UP, (float) ((-target.toYRot() - 90) / 180 * Math.PI));
	}

}
