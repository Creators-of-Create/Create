package com.simibubi.create.lib.mixin.common;

import com.mojang.serialization.Codec;

import com.simibubi.create.lib.utility.BiomeUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;

@Mixin(RegistryAccess.class)
public class RegistryAccessMixin {
//	@ModifyArg(method = "lambda$static$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistryAccess;put(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)V", ordinal = 1), index = 2)
//	private static Codec<Biome> modifyCodec(Codec<Biome> elementCodec) {
//		return BiomeUtil.DIRECT_CODEC;
//	}
}
