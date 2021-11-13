package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

@Mixin(Biome.class)
public abstract class BiomeMixin {
	@Mutable
	@Shadow
	@Final
	private BiomeGenerationSettings generationSettings;

	@Inject(at = @At("TAIL"), method = "<init>")
	public void create$biomeInit(Biome.ClimateSettings climate, Biome.BiomeCategory category, float f, float g, BiomeSpecialEffects biomeAmbience, BiomeGenerationSettings biomeGenerationSettings, MobSpawnSettings mobSpawnInfo, CallbackInfo ci) {
		ResourceLocation key = BuiltinRegistries.BIOME.getKey((Biome) (Object) this); // dunno
//		this.generationSettings = BiomeLoadingCallback.EVENT.invoker().onBiomeLoad(key, category, BiomeUtil.settingsToBuilder(biomeGenerationSettings)).build();
	}
}
