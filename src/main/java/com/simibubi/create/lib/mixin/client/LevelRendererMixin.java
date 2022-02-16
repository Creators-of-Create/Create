package com.simibubi.create.lib.mixin.client;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import com.simibubi.create.lib.block.CullingBlockEntityIterator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	private Frustum cullingFrustum;

	@Shadow
	@Nullable
	private Frustum capturedFrustum;

	@ModifyVariable(
		method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V",
		slice = @Slice(
				from = @At(
						value = "INVOKE",
						target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;getRenderableBlockEntities()Ljava/util/List;"
				),
				to = @At(
						value = "INVOKE",
						target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"
				)
		),
		at = @At("STORE")
	)
	private Iterator<BlockEntity> wrapBlockEntityIterator(Iterator<BlockEntity> iterator) {
		return new CullingBlockEntityIterator(iterator, capturedFrustum != null ? capturedFrustum : cullingFrustum);
	}
}
