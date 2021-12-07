package com.simibubi.create.foundation.worldgen;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class AllWorldFeatures {

	public static final Map<ResourceLocation, ConfigDrivenFeatureEntry> ENTRIES = new HashMap<>();

	private static final BiomeFilter OVERWORLD_BIOMES =
		(r, b) -> b != BiomeCategory.NETHER && b != BiomeCategory.THEEND && b != BiomeCategory.NONE;

	private static final BiomeFilter NETHER_BIOMES = (r, b) -> b == BiomeCategory.NETHER;

	//

	public static final ConfigDrivenFeatureEntry ZINC_ORE = register("zinc_ore", 12, 8, OVERWORLD_BIOMES).between(-63, 70)
		.withBlocks(Couple.create(AllBlocks.ZINC_ORE, AllBlocks.DEEPSLATE_ZINC_ORE));

	public static final ConfigDrivenFeatureEntry STRIATED_ORES_OVERWORLD =
		register("striated_ores_overworld", 32, 1 / 12f, OVERWORLD_BIOMES).between(-30, 70)
			.withLayerPattern(AllLayerPatterns.SCORIA)
			.withLayerPattern(AllLayerPatterns.CINNABAR)
			.withLayerPattern(AllLayerPatterns.MAGNETITE)
			.withLayerPattern(AllLayerPatterns.MALACHITE)
			.withLayerPattern(AllLayerPatterns.LIMESTONE)
			.withLayerPattern(AllLayerPatterns.OCHRESTONE);

	public static final ConfigDrivenFeatureEntry STRIATED_ORES_NETHER =
		register("striated_ores_nether", 32, 1 / 12f, NETHER_BIOMES).between(40, 90)
			.withLayerPattern(AllLayerPatterns.SCORIA_NETHER)
			.withLayerPattern(AllLayerPatterns.SCORCHIA_NETHER);

	//

	private static ConfigDrivenFeatureEntry register(String id, int clusterSize, float frequency,
		BiomeFilter biomeFilter) {
		ConfigDrivenFeatureEntry configDrivenFeatureEntry = new ConfigDrivenFeatureEntry(id, clusterSize, frequency);
		configDrivenFeatureEntry.biomeFilter = biomeFilter;
		ENTRIES.put(Create.asResource(id), configDrivenFeatureEntry);
		return configDrivenFeatureEntry;
	}

	/**
	 * Increment this number if all worldgen entries should be overwritten in this
	 * update. Worlds from the previous version will overwrite potentially changed
	 * values with the new defaults.
	 */
	public static final int forcedUpdateVersion = 2;

	public static void registerFeatures() {
		ENTRIES.entrySet()
			.forEach(entry -> {
				String id = Create.ID + "_" + entry.getKey()
					.getPath();
				ConfigDrivenFeatureEntry value = entry.getValue();
				Pair<ConfiguredFeature<?, ?>, PlacedFeature> feature = value.getFeature();
				Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature.getFirst());
				Registry.register(BuiltinRegistries.PLACED_FEATURE, id, feature.getSecond());
			});
	}

	public static void reload(BiomeLoadingEvent event) {
		BiomeGenerationSettingsBuilder generation = event.getGeneration();
		Decoration decoStep = GenerationStep.Decoration.UNDERGROUND_ORES;
		ENTRIES.values()
			.forEach(entry -> {
				if (!entry.biomeFilter.test(event.getName(), event.getCategory()))
					return;
				generation.addFeature(decoStep, entry.getFeature()
					.getSecond());
			});
	}

	public static void fillConfig(ForgeConfigSpec.Builder builder) {
		ENTRIES.values()
			.forEach(entry -> {
				builder.push(entry.id);
				entry.addToConfig(builder);
				builder.pop();
			});
	}

	public static void register() {}

	public static void registerOreFeatures(RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry()
			.registerAll(VanillaStyleOreFeature.INSTANCE, LayeredOreFeature.INSTANCE);
	}

	public static void registerPlacementTypes() {
		ConfigDrivenDecorator.TYPE =
			Registry.register(Registry.PLACEMENT_MODIFIERS, "create_config_driven", () -> ConfigDrivenDecorator.CODEC);
	}

}
