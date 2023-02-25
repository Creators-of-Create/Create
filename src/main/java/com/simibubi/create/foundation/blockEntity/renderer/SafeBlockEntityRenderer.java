package com.simibubi.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
	@Override
	public final void render(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light,
		int overlay) {
		if (isInvalid(be))
			return;
		renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
	}

	protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light,
		int overlay);

	public boolean isInvalid(T be) {
		return !be.hasLevel() || be.getBlockState()
			.getBlock() == Blocks.AIR;
	}
}
