package com.simibubi.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

	public boolean shouldCullItem(Vec3 itemPos) {
		Frustum frustum = Minecraft.getInstance().levelRenderer.capturedFrustum != null ?
				Minecraft.getInstance().levelRenderer.capturedFrustum :
				Minecraft.getInstance().levelRenderer.cullingFrustum;

		AABB itemBB = new AABB(
				itemPos.x - 0.25,
				itemPos.y - 0.25,
				itemPos.z - 0.25,
				itemPos.x + 0.25,
				itemPos.y + 0.25,
				itemPos.z + 0.25
		);

		return !frustum.isVisible(itemBB);
	}
}
