package com.simibubi.create.foundation.tileEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SafeTileEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
	@Override
	public final void render(T te, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light,
		int overlay) {
		if (isInvalid(te))
			return;
		renderSafe(te, partialTicks, ms, bufferSource, light, overlay);
	}

	protected abstract void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light,
		int overlay);

	public boolean isInvalid(T te) {
		return !te.hasLevel() || te.getBlockState()
			.getBlock() == Blocks.AIR;
	}
}
