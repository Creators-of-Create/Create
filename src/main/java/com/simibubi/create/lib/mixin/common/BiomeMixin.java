package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;

@Mixin(Biome.class)
public abstract class BiomeMixin {
	@Mutable
	@Shadow
	@Final
	private BiomeGenerationSettings generationSettings;

//	@Inject(at = @At("TAIL"), method = "<init>")
//	public void create$biomeInit(Biome.ClimateSettings climateSettings, Biome.BiomeCategory biomeCategory, BiomeSpecialEffects biomeSpecialEffects, BiomeGenerationSettings biomeGenerationSettfings, MobSpawnSettings mobSpawnSettings, CallbackInfo ci) {
//		ResourceLocation key = BuiltinRegistries.BIOME.getKey((Biome) (Object) this); // dunno
////		if(biomeGenerationSettings != BiomeGenerationSettings.EMPTY) {
//			List<List<Supplier<PlacedFeature>>> features = new ArrayList<>();
//			BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
//		generationSettings.features().forEach(nestedFeatures -> nestedFeatures.forEach(nestedFeature -> features.add(Lists.newArrayList(nestedFeature))));
//			((BiomeGenerationSettings$BuilderAccessor)builder).setFeatures(features);
//			BiomeLoadingCallback.EVENT.invoker().onBiomeLoad(BuiltinRegistries.BIOME.getKey(MixinHelper.cast(this)), biomeCategory, builder);
//			((BiomeGenerationSettingsAccessor)generationSettings).setFeatures(builder.build().features());
////		}
//			//this.generationSettings = BiomeLoadingCallback.EVENT.invoker().onBiomeLoad(key, biomeCategory,  );
//	}

}
