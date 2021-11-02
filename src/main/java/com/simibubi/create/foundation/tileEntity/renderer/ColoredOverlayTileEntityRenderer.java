package com.simibubi.create.foundation.tileEntity.renderer;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ColoredOverlayTileEntityRenderer<T extends BlockEntity> extends SafeTileEntityRenderer<T> {

	public ColoredOverlayTileEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		SuperByteBuffer render = render(getOverlayBuffer(te), getColor(te, partialTicks), light);
		render.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	protected abstract int getColor(T te, float partialTicks);

	protected abstract SuperByteBuffer getOverlayBuffer(T te);

	public static SuperByteBuffer render(SuperByteBuffer buffer, int color, int light) {
		return buffer.color(color).light(light);
	}

}
