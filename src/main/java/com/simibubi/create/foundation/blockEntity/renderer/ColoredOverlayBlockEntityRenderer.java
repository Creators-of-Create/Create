package com.simibubi.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ColoredOverlayBlockEntityRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {

	public ColoredOverlayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {

		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		SuperByteBuffer render = render(getOverlayBuffer(be), getColor(be, partialTicks), light);
		render.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

	protected abstract int getColor(T be, float partialTicks);

	protected abstract SuperByteBuffer getOverlayBuffer(T be);

	public static SuperByteBuffer render(SuperByteBuffer buffer, int color, int light) {
		return buffer.color(color).light(light);
	}

}
