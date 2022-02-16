package com.simibubi.create.content.logistics.trains.management;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.management.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StationRenderer extends SafeTileEntityRenderer<StationTileEntity> {

	public StationRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(StationTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		BlockPos pos = te.getBlockPos();
		TrackTargetingBehaviour target = te.getTarget();
		BlockPos targetPosition = target.getGlobalPosition();
		Level level = te.getLevel();

		BlockState trackState = level.getBlockState(targetPosition);
		Block block = trackState.getBlock();
		if (!(block instanceof ITrackBlock))
			return;

		GlobalStation station = te.getOrCreateGlobalStation();

		if (!te.getBlockState()
			.getValue(StationBlock.ASSEMBLING) || station == null || station.getPresentTrain() != null) {
			ms.pushPose();
			ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
			TrackTargetingBehaviour.render(level, targetPosition, target.getTargetDirection(), 0xCC993B, ms, buffer,
				light, overlay, RenderedTrackOverlayType.STATION);
			ms.popPose();
			return;
		}

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

	@Override
	public boolean shouldRenderOffScreen(StationTileEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
