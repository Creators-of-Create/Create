package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.Transform;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StationRenderer extends SafeTileEntityRenderer<StationTileEntity> {

	public StationRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(StationTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		BlockPos pos = te.getBlockPos();
		TrackTargetingBehaviour<GlobalStation> target = te.edgePoint;
		BlockPos targetPosition = target.getGlobalPosition();
		Level level = te.getLevel();
		ItemStack autoSchedule = te.getAutoSchedule();

		if (!autoSchedule.isEmpty())
			renderItem(autoSchedule, te, partialTicks, ms, buffer, light, overlay);

		BlockState trackState = level.getBlockState(targetPosition);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		GlobalStation station = te.getStation();

		if (!te.getBlockState()
			.getValue(StationBlock.ASSEMBLING) || station == null || station.getPresentTrain() != null) {
			renderFlag(
				te.flag.getValue(partialTicks) > 0.75f ? AllBlockPartials.STATION_ON : AllBlockPartials.STATION_OFF, te,
				partialTicks, ms, buffer, light, overlay);
			ms.pushPose();
			ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
			TrackTargetingBehaviour.render(level, targetPosition, target.getTargetDirection(), target.getTargetBezier(),
				0xCC993B, ms, buffer, light, overlay, RenderedTrackOverlayType.STATION);
			ms.popPose();
			return;
		}

		renderFlag(AllBlockPartials.STATION_ASSEMBLE, te, partialTicks, ms, buffer, light, overlay);

		ITrackBlock track = (ITrackBlock) block;
		Direction direction = te.assemblyDirection;

		if (direction == null || te.assemblyLength == 0 || te.bogeyLocations == null)
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

		for (int i = 0; i < te.assemblyLength; i++) {
			int valid = te.isValidBogeyOffset(i) ? colorWhenValid : -1;

			for (int j : te.bogeyLocations)
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

	public static void renderItem(ItemStack itemStack, StationTileEntity te, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		TransformStack msr = TransformStack.cast(ms);

		ms.pushPose();
		msr.centre();

		Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
		if (renderViewEntity != null) {
			Vec3 positionVec = renderViewEntity.position();
			Vec3 vectorForOffset = Vec3.atCenterOf(te.getBlockPos());
			Vec3 diff = vectorForOffset.subtract(positionVec);
			float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
			ms.mulPose(Vector3f.YP.rotation(yRot));
		}

		ms.translate(0, 10 / 32d, 0);
		ms.scale(.75f, .75f, .75f);
		itemRenderer.renderStatic(itemStack, TransformType.FIXED, light, overlay, ms, buffer, 0);

		ms.popPose();
	}

	public static void renderFlag(PartialModel flag, StationTileEntity te, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		if (!te.resolveFlagAngle())
			return;
		SuperByteBuffer flagBB = CachedBufferer.partial(flag, te.getBlockState());
		transformFlag(flagBB, te, partialTicks, te.flagYRot, te.flagFlipped);
		flagBB.translate(0.5f / 16, 0, 0)
			.rotateY(te.flagFlipped ? 0 : 180)
			.translate(-0.5f / 16, 0, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

	public static void transformFlag(Transform<?> flag, StationTileEntity te, float partialTicks, int yRot,
		boolean flipped) {
		float value = te.flag.getValue(partialTicks);
		float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
		if (te.flag.getChaseTarget() > 0 && !te.flag.settled() && progress == 1) {
			float wiggleProgress = (value - .2f) / .8f;
			progress += (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
		}

		flag.centre()
			.rotateY(yRot)
			.translate(1 / 64f, 4.5f / 16f, flipped ? 13.5f / 16f : 2.5f / 16f)
			.unCentre()
			.rotateX((flipped ? 1 : -1) * (progress * 60 + 300));
	}

	@Override
	public boolean shouldRenderOffScreen(StationTileEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
