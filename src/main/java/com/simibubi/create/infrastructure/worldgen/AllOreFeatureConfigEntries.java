package com.simibubi.create.infrastructure.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.DynamicDataProvider;
import com.simibubi.create.infrastructure.worldgen.OreFeatureConfigEntry.DatagenExtension;

import net.createmod.catnip.utility.Couple;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class AllOreFeatureConfigEntries {
	private static final Predicate<BiomeLoadingEvent> OVERWORLD_BIOMES = event -> {
		Biome.BiomeCategory category = event.getCategory();
		return category != BiomeCategory.NETHER && category != BiomeCategory.THEEND && category != BiomeCategory.NONE;
	};

	private static final Predicate<BiomeLoadingEvent> NETHER_BIOMES = event -> {
		Biome.BiomeCategory category = event.getCategory();
		return category == BiomeCategory.NETHER;
	};

	//

	public static final OreFeatureConfigEntry ZINC_ORE =
		create("zinc_ore", 12, 8, -63, 70)
			.biomeExt()
			.predicate(OVERWORLD_BIOMES)
			.parent()
			.standardDatagenExt()
			.withBlocks(Couple.create(AllBlocks.ZINC_ORE, AllBlocks.DEEPSLATE_ZINC_ORE))
			.parent();

	public static final OreFeatureConfigEntry STRIATED_ORES_OVERWORLD =
		create("striated_ores_overworld", 32, 1 / 18f, -30, 70)
			.biomeExt()
			.predicate(OVERWORLD_BIOMES)
			.parent()
			.layeredDatagenExt()
			.withLayerPattern(AllLayerPatterns.SCORIA)
			.withLayerPattern(AllLayerPatterns.CINNABAR)
			.withLayerPattern(AllLayerPatterns.MAGNETITE)
			.withLayerPattern(AllLayerPatterns.MALACHITE)
			.withLayerPattern(AllLayerPatterns.LIMESTONE)
			.withLayerPattern(AllLayerPatterns.OCHRESTONE)
			.parent();

	public static final OreFeatureConfigEntry STRIATED_ORES_NETHER =
		create("striated_ores_nether", 32, 1 / 18f, 40, 90)
			.biomeExt()
			.predicate(NETHER_BIOMES)
			.parent()
			.layeredDatagenExt()
			.withLayerPattern(AllLayerPatterns.SCORIA_NETHER)
			.withLayerPattern(AllLayerPatterns.SCORCHIA_NETHER)
			.parent();

	//

	private static OreFeatureConfigEntry create(String name, int clusterSize, float frequency,
		int minHeight, int maxHeight) {
		ResourceLocation id = Create.asResource(name);
		OreFeatureConfigEntry configDrivenFeatureEntry = new OreFeatureConfigEntry(id, clusterSize, frequency, minHeight, maxHeight);
		return configDrivenFeatureEntry;
	}

	public static void fillConfig(ForgeConfigSpec.Builder builder, String namespace) {
		OreFeatureConfigEntry.ALL
			.forEach((id, entry) -> {
				if (id.getNamespace().equals(namespace)) {
					builder.push(entry.getName());
					entry.addToConfig(builder);
					builder.pop();
				}
			});
	}

	public static void init() {}

	public static void modifyBiomes(BiomeLoadingEvent event) {
		for (OreFeatureConfigEntry entry : OreFeatureConfigEntry.ALL.values()) {
			entry.biomeExt().modifyBiomes(event, BuiltinRegistries.PLACED_FEATURE);
		}
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		RegistryAccess registryAccess = RegistryAccess.BUILTIN.get();

		//

		Map<ResourceLocation, ConfiguredFeature<?, ?>> configuredFeatures = new HashMap<>();
		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
			DatagenExtension datagenExt = entry.getValue().datagenExt();
			if (datagenExt != null) {
				configuredFeatures.put(entry.getKey(), datagenExt.createConfiguredFeature(registryAccess));
			}
		}

		DynamicDataProvider<ConfiguredFeature<?, ?>> configuredFeatureProvider = DynamicDataProvider.create(generator, "Create's Configured Features", registryAccess, Registry.CONFIGURED_FEATURE_REGISTRY, configuredFeatures);
		if (configuredFeatureProvider != null) {
			generator.addProvider(configuredFeatureProvider);
		}

		//

		Map<ResourceLocation, PlacedFeature> placedFeatures = new HashMap<>();
		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
			DatagenExtension datagenExt = entry.getValue().datagenExt();
			if (datagenExt != null) {
				placedFeatures.put(entry.getKey(), datagenExt.createPlacedFeature(registryAccess));
			}
		}

		DynamicDataProvider<PlacedFeature> placedFeatureProvider = DynamicDataProvider.create(generator, "Create's Placed Features", registryAccess, Registry.PLACED_FEATURE_REGISTRY, placedFeatures);
		if (placedFeatureProvider != null) {
			generator.addProvider(placedFeatureProvider);
		}
	}
}
