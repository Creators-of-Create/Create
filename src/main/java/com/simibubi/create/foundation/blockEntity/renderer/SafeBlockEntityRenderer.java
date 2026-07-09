package com.simibubi.create.foundation.blockEntity.renderer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;

import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, SafeBlockEntityRenderer.SafeBlockEntityRenderState<T>> {
	public static class SafeBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
		T blockEntity;
		float partialTicks;
		int light;
		int overlay;
	}

	@Override
	public SafeBlockEntityRenderState<T> createRenderState() {
		return new SafeBlockEntityRenderState<>();
	}

	@Override
	public void extractRenderState(T blockEntity, SafeBlockEntityRenderState<T> state, float partialTicks,
		Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTicks = partialTicks;
		state.light = state.lightCoords;
		state.overlay = 0;
	}

	@Override
	public final void submit(SafeBlockEntityRenderState<T> state, PoseStack ms, SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera) {
		// TODO 26.2: Port the old MultiBufferSource renderers to SubmitNodeCollector.
	}

	protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light,
		int overlay);

	public boolean isInvalid(T be) {
		return !be.hasLevel() || be.getBlockState()
			.getBlock() == Blocks.AIR;
	}

	public boolean shouldCullItem(Vec3 itemPos, Level level) {
		if (level instanceof PonderLevel)
			return false;

		Camera camera = Minecraft.getInstance()
			.gameRenderer
			.mainCamera();
		Frustum capturedFrustum = camera.getCapturedFrustum();
		Frustum frustum = capturedFrustum != null ? capturedFrustum : camera.getCullFrustum();

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

	@Override
	public @NotNull AABB getRenderBoundingBox(@NotNull T blockEntity) {
		if (blockEntity instanceof CachedRenderBBBlockEntity cbe)
			return cbe.getRenderBoundingBox();

		return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
	}
}
