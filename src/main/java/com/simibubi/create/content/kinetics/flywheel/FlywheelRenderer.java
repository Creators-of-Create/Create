package com.simibubi.create.content.kinetics.flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FlywheelRenderer extends KineticBlockEntityRenderer<FlywheelBlockEntity> {

	public FlywheelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FlywheelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();

		float speed = be.visualSpeed.getValue(partialTicks) * 3 / 10f;
		float angle = be.angle + speed * partialTicks;

		VertexConsumer vb = buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid());
		renderFlywheel(be, ms, light, blockState, angle, vb);
	}

	private void renderFlywheel(FlywheelBlockEntity be, PoseStack ms, int light, BlockState blockState, float angle,
		VertexConsumer vb) {
		SuperByteBuffer wheel = CachedBuffers.block(blockState);
		kineticRotationTransform(wheel, be, getRotationAxisOf(be), AngleHelper.rad(angle), light);
		wheel.renderInto(ms, vb);
	}

	@Override
	protected BlockState getRenderedBlockState(FlywheelBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
