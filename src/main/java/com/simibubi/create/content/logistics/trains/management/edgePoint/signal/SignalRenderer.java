package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalTileEntity.OverlayState;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalTileEntity.SignalState;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SignalRenderer extends SafeTileEntityRenderer<SignalTileEntity> {

	public SignalRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SignalTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		SignalState signalState = te.getState();
		OverlayState overlayState = te.getOverlay();

		float renderTime = AnimationTickHolder.getRenderTime(te.getLevel());
		if (signalState.isRedLight(renderTime))
			CachedBufferer.partial(AllBlockPartials.SIGNAL_ON, blockState)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		else
			CachedBufferer.partial(AllBlockPartials.SIGNAL_OFF, blockState)
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		BlockPos pos = te.getBlockPos();
		TrackTargetingBehaviour<SignalBoundary> target = te.edgePoint;
		BlockPos targetPosition = target.getGlobalPosition();
		Level level = te.getLevel();
		BlockState trackState = level.getBlockState(targetPosition);
		Block block = trackState.getBlock();

		if (!(block instanceof ITrackBlock))
			return;
		if (overlayState == OverlayState.SKIP)
			return;

		ms.pushPose();
		TransformStack.cast(ms)
			.translate(targetPosition.subtract(pos));
		RenderedTrackOverlayType type =
			overlayState == OverlayState.DUAL ? RenderedTrackOverlayType.DUAL_SIGNAL : RenderedTrackOverlayType.SIGNAL;
		TrackTargetingBehaviour.render(level, targetPosition, target.getTargetDirection(), target.getTargetBezier(), ms,
			buffer, light, overlay, type, 1);
		ms.popPose();

	}

}
