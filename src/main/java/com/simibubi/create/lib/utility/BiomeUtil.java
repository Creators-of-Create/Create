package com.simibubi.create.lib.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.lib.mixin.accessor.BiomeGenerationSettings$BuilderAccessor;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class BiomeUtil {

//	public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create((p_186636_) -> {
//		return p_186636_.group(Biome.ClimateSettings.CODEC.forGetter((biome) -> {
//					return ((BiomeAccessor)(Object)biome).getClimateSettings();
//				}), Biome.BiomeCategory.CODEC.fieldOf("category").forGetter((biome) -> {
//					return biome.getBiomeCategory();
//				}), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((biome) -> {
//					return biome.getSpecialEffects();
//				}), BiomeGenerationSettings.CODEC.forGetter((biome) -> {
//					return biome.getGenerationSettings();
//				}), MobSpawnSettings.CODEC.forGetter((biome) -> {
//					return biome.getMobSettings();
//				}), ResourceLocation.CODEC.optionalFieldOf("fabric:registry_name").forGetter(b -> Optional.ofNullable(BuiltinRegistries.BIOME.getKey(b))))
//				.apply(p_186636_, ((climateSettings, biomeCategory, biomeSpecialEffects, biomeGenerationSettings, mobSpawnSettings, resourceLocation) -> {
//					List<List<Supplier<PlacedFeature>>> features = new ArrayList<>();
//					BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
//					biomeGenerationSettings.features().forEach(nestedFeatures -> nestedFeatures.forEach(nestedFeature -> features.add(Lists.newArrayList(nestedFeature))));
//					((BiomeGenerationSettings$BuilderAccessor)builder).setFeatures(features);
//					BiomeLoadingCallback.EVENT.invoker().onBiomeLoad(resourceLocation.orElse(null), biomeCategory, builder);
//					((BiomeGenerationSettingsAccessor)biomeGenerationSettings).setFeatures(builder.build().features());
//					//BiomeGenerationSettingsAccessor.createBiomeGenerationSettings(((BiomeGenerationSettingsAccessor)biomeGenerationSettings).getCarvers(), features);
//					return BiomeAccessor.createBiome(climateSettings, biomeCategory, biomeSpecialEffects, biomeGenerationSettings, mobSpawnSettings);
//				}));
//	});

	public static BiomeGenerationSettings.Builder settingsToBuilder(BiomeGenerationSettings settings) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		BiomeGenerationSettings$BuilderAccessor builderAccessor = MixinHelper.cast(builder);
		Collections.unmodifiableSet(builderAccessor.getCarvers().keySet()).forEach(c -> {
			Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> newCarvers = builderAccessor.getCarvers();
			newCarvers.put(c, new ArrayList<>(settings.getCarvers(c)));
			builderAccessor.setCarvers(newCarvers);
		});
		builderAccessor.getFeatures().forEach(f -> {
			List<List<Supplier<PlacedFeature>>> newFeatures = builderAccessor.getFeatures();
			newFeatures.add(new ArrayList<>(f));
			builderAccessor.setFeatures(newFeatures);
		});
		return builder;
	}
}
