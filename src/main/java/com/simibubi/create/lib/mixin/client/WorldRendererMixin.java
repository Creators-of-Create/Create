package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
//	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;push()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD,
//			method = "Lnet/minecraft/client/renderer/WorldRenderer;render(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
//	public void render(MatrixStack matrixStack, float f, long l, boolean bl, ActiveRenderInfo activeRenderInfo, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci, ClippingHelper clippinghelper, TileEntity tileEntity2) {
//		if (tileEntity2 instanceof CustomRenderBoundingBox) {
//			if (clippinghelper.isVisible(((CustomRenderBoundingBox) tileEntity2).getRenderBoundingBox())) {
//				continue outsideLoop;
//			}
//		}
//	}

//	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD,
//			method = "Lnet/minecraft/client/renderer/WorldRenderer;render(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
//	public void addLabel(MatrixStack matrixStack, float f, long l, boolean bl, ActiveRenderInfo activeRenderInfo, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci, ClippingHelper clippinghelper, TileEntity tileEntity2) {
//		if (tileEntity2 instanceof CustomRenderBoundingBox) {
//			if (clippinghelper.isVisible(((CustomRenderBoundingBox) tileEntity2).getRenderBoundingBox())) {
//				outsideLoop:
//				for (int i = 0; i == 0; i++) {
//					// kill me
//				}
//			}
//		}
//	}
}
