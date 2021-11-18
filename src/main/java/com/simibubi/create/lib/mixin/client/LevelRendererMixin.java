package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.lib.block.CustomRenderBoundingBox;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	private @Nullable Frustum capturedFrustum;

	@Shadow
	private Frustum cullingFrustum;

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
}
