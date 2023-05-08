package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.Transform;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.block.depot.DepotRenderer;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StationRenderer extends SafeBlockEntityRenderer<StationBlockEntity> {

	public StationRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(StationBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		BlockPos pos = be.getBlockPos();
		TrackTargetingBehaviour<GlobalStation> target = be.edgePoint;
		BlockPos targetPosition = target.getGlobalPosition();
		Level level = be.getLevel();

		DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.depotBehaviour);

		BlockState trackState = level.getBlockState(targetPosition);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		GlobalStation station = be.getStation();
		boolean isAssembling = be.getBlockState()
			.getValue(StationBlock.ASSEMBLING);
		
		if (!isAssembling || (station == null || station.getPresentTrain() != null) && !be.isVirtual()) {
			renderFlag(
				be.flag.getValue(partialTicks) > 0.75f ? AllPartialModels.STATION_ON : AllPartialModels.STATION_OFF, be,
				partialTicks, ms, buffer, light, overlay);
			ms.pushPose();
			TransformStack.cast(ms)
				.translate(targetPosition.subtract(pos));
			TrackTargetingBehaviour.render(level, targetPosition, target.getTargetDirection(), target.getTargetBezier(),
				ms, buffer, light, overlay, RenderedTrackOverlayType.STATION, 1);
			ms.popPose();
			return;
		}

		renderFlag(AllPartialModels.STATION_ASSEMBLE, be, partialTicks, ms, buffer, light, overlay);

		ITrackBlock track = (ITrackBlock) block;
		Direction direction = be.assemblyDirection;

		if (be.isVirtual() && be.bogeyLocations == null)
			be.refreshAssemblyInfo();
		
		if (direction == null || be.assemblyLength == 0 || be.bogeyLocations == null)
			return;

		ms.pushPose();
		BlockPos offset = targetPosition.subtract(pos);
		ms.translate(offset.getX(), offset.getY(), offset.getZ());

		MutableBlockPos currentPos = targetPosition.mutable();

		PartialModel assemblyOverlay = track.prepareAssemblyOverlay(level, targetPosition, trackState, direction, ms);
		int colorWhenValid = 0x96B5FF;
		int colorWhenCarriage = 0xCAFF96;
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

		currentPos.move(direction, 1);
		ms.translate(0, 0, 1);

		for (int i = 0; i < be.assemblyLength; i++) {
			int valid = be.isValidBogeyOffset(i) ? colorWhenValid : -1;

			for (int j : be.bogeyLocations)
				if (i == j) {
					valid = colorWhenCarriage;
					break;
				}

			if (valid != -1) {
				int lightColor = LevelRenderer.getLightColor(level, currentPos);
				SuperByteBuffer sbb = CachedBufferer.partial(assemblyOverlay, trackState);
				sbb.color(valid);
				sbb.light(lightColor);
				sbb.renderInto(ms, vb);
			}
			ms.translate(0, 0, 1);
			currentPos.move(direction);
		}

		ms.popPose();
	}

	public static void renderFlag(PartialModel flag, StationBlockEntity be, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		if (!be.resolveFlagAngle())
			return;
		SuperByteBuffer flagBB = CachedBufferer.partial(flag, be.getBlockState());
		transformFlag(flagBB, be, partialTicks, be.flagYRot, be.flagFlipped);
		flagBB.translate(0.5f / 16, 0, 0)
			.rotateY(be.flagFlipped ? 0 : 180)
			.translate(-0.5f / 16, 0, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

	public static void transformFlag(Transform<?> flag, StationBlockEntity be, float partialTicks, int yRot,
		boolean flipped) {
		float value = be.flag.getValue(partialTicks);
		float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
		if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
			float wiggleProgress = (value - .2f) / .8f;
			progress += (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
		}

		float nudge = 1 / 512f;
		flag.centre()
			.rotateY(yRot)
			.translate(nudge, 9.5f / 16f, flipped ? 14f / 16f - nudge : 2f / 16f + nudge)
			.unCentre()
			.rotateX((flipped ? 1 : -1) * (progress * 90 + 270));
	}

	@Override
	public boolean shouldRenderOffScreen(StationBlockEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
