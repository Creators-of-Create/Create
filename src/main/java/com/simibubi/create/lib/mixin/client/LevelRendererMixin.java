package com.simibubi.create.lib.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.simibubi.create.lib.block.CustomRenderBoundingBox;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.simibubi.create.lib.extensions.AbstractTextureExtension;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	private @Nullable Frustum capturedFrustum;

	@Shadow
	private Frustum cullingFrustum;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(
			method = "renderLevel",
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
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
			)
	)
	private <E> E redirectBlockEntityIterator(Iterator<E> instance) {
		E obj = instance.next();
		BlockEntity next = (BlockEntity) obj;
		if (next instanceof CustomRenderBoundingBox custom) {
			Frustum frustum = capturedFrustum != null ? capturedFrustum : cullingFrustum;
			// skip this BE if not visible
			if (!frustum.isVisible(custom.getRenderBoundingBox())) {
				// if this is the last BE in the list, we need to render anyway to avoid an index out of bounds
				if (!instance.hasNext()) {
					return obj;
				}
				return redirectBlockEntityIterator(instance);
			}
		}
		return obj;
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V", ordinal = 0))
	public void setBur(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		((AbstractTextureExtension)this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)).setBlurMipmap(false, this.minecraft.options.mipmapLevels > 0);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V", ordinal = 1))
	public void lastBlur	(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		((AbstractTextureExtension)this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)).restoreLastBlurMipmap();
	}
}
