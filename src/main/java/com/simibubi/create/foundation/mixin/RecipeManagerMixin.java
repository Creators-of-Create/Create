package com.simibubi.create.foundation.mixin;

import com.google.gson.JsonElement;

import com.llamalad7.mixinextras.sugar.Local;

import com.simibubi.create.foundation.codec.ResourceLocationAwareOps;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;")
	)
	private void create$setResourceLocation(
		Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler,
		CallbackInfo ci, @Local ResourceLocation resourcelocation, @Local RegistryOps<JsonElement> registryops
	) {
		if (registryops instanceof ResourceLocationAwareOps awareOps) {
			awareOps.setResourceLocation(resourcelocation);
		}
	}
}
