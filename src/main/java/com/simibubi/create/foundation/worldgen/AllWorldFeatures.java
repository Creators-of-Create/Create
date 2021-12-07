package com.simibubi.create.foundation.worldgen;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.ForgeConfigSpec;

public class AllWorldFeatures {

	public static final Map<ResourceLocation, ConfigDrivenFeatureEntry> ENTRIES = new HashMap<>();

	private static final BiomeFilter OVERWORLD_BIOMES =
		(r, b) -> b != BiomeCategory.NETHER && b != BiomeCategory.THEEND && b != BiomeCategory.NONE;

	private static final BiomeFilter NETHER_BIOMES = (r, b) -> b == BiomeCategory.NETHER;

	//

	public static final ConfigDrivenFeatureEntry ZINC_ORE = register("zinc_ore", 12, 3, OVERWORLD_BIOMES).between(1, 70)
		.withBlocks(Couple.create(AllBlocks.ZINC_ORE, AllBlocks.DEEPSLATE_ZINC_ORE));

	public static final ConfigDrivenFeatureEntry STRIATED_ORES_OVERWORLD =
		register("striated_ores_overworld", 32, 1 / 18f, OVERWORLD_BIOMES).between(1, 70)
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
				Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, Create.ID + "_" + entry.getKey()
					.getPath(), entry.getValue()
						.getFeature());
			});
	}

	public static BiomeGenerationSettings.Builder reload(ResourceLocation key, Biome.BiomeCategory category, BiomeGenerationSettings.Builder generation) {
		ENTRIES.values()
			.forEach(entry -> {
				if (key == Biomes.THE_VOID.location()) // uhhh???
					return;
				if (category == BiomeCategory.NETHER)
					return;
				generation
						.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, entry.getFeature());
			});
		return generation;
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

	public static void registerOreFeatures() {
		Registry.register(Registry.FEATURE, VanillaStyleOreFeature.ID, VanillaStyleOreFeature.INSTANCE);
		Registry.register(Registry.FEATURE, LayeredOreFeature.ID, LayeredOreFeature.INSTANCE);
	}

	public static void registerDecoratorFeatures() {
		Registry.register(Registry.DECORATOR, ConfigDrivenDecorator.ID, ConfigDrivenDecorator.INSTANCE);
	}

}
