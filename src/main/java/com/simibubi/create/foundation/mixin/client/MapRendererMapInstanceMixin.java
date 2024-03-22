package com.simibubi.create.foundation.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.map.CustomRenderedMapDecoration;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.MapRenderer$MapInstance")
public class MapRendererMapInstanceMixin {
	@Shadow
	private MapItemSavedData data;

	@Group(name = "custom_decoration_rendering", min = 1, max = 1)
	@Inject(method = "draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapDecoration;render(I)Z", remap = false))
	private void create$onDraw(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, CallbackInfo ci, @Local(ordinal = 3) int index, @Local MapDecoration decoration) {
		if (decoration instanceof CustomRenderedMapDecoration renderer) {
			renderer.render(poseStack, bufferSource, active, packedLight, data, index);
		}
	}

	@Group(name = "custom_decoration_rendering")
	@Inject(method = "draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ZI)V", at = @At(value = "FIELD", target = "net/optifine/reflect/Reflector.ForgeMapDecoration_render:Lnet/optifine/reflect/ReflectorMethod;", opcode = Opcodes.GETSTATIC, ordinal = 1, remap = false))
	private void create$onDrawOptifine(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, CallbackInfo ci, @Local(ordinal = 3) int index, @Local MapDecoration decoration) {
		if (decoration instanceof CustomRenderedMapDecoration renderer) {
			renderer.render(poseStack, bufferSource, active, packedLight, data, index);
		}
	}
}
