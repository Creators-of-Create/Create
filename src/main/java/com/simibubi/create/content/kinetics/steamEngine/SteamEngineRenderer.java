package com.simibubi.create.content.kinetics.steamEngine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class SteamEngineRenderer extends SafeBlockEntityRenderer<SteamEngineBlockEntity> {

	public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SteamEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		Float angle = be.getTargetAngle();
		if (angle == null)
			return;

		BlockState blockState = be.getBlockState();
		Direction facing = SteamEngineBlock.getFacing(blockState);
		Axis facingAxis = facing.getAxis();
		Axis axis = Axis.Y;

		PoweredShaftBlockEntity shaft = be.getShaft();
		if (shaft != null)
			axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

		boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
		float sine = Mth.sin(angle);
		float sine2 = Mth.sin(angle - Mth.HALF_PI);
		float piston = ((1 - sine) / 4) * 24 / 16f;

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		transformed(AllPartialModels.ENGINE_PISTON, blockState, facing, roll90)
			.translate(0, piston, 0)
			.light(light)
			.renderInto(ms, vb);

		transformed(AllPartialModels.ENGINE_LINKAGE, blockState, facing, roll90)
			.center()
			.translate(0, 1, 0)
			.uncenter()
			.translate(0, piston, 0)
			.translate(0, 4 / 16f, 8 / 16f)
			.rotateXDegrees(sine2 * 23f)
			.translate(0, -4 / 16f, -8 / 16f)
			.light(light)
			.renderInto(ms, vb);

		transformed(AllPartialModels.ENGINE_CONNECTOR, blockState, facing, roll90)
			.translate(0, 2, 0)
			.center()
			.rotateX(-angle + Mth.HALF_PI)
			.uncenter()
			.light(light)
			.renderInto(ms, vb);
	}

	private SuperByteBuffer transformed(PartialModel model, BlockState blockState, Direction facing, boolean roll90) {
		return CachedBufferer.partial(model, blockState)
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.rotateYDegrees(roll90 ? -90 : 0)
			.uncenter();
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

}
