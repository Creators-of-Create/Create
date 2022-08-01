package com.simibubi.create.foundation.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegisterEvent;

public class AllWorldFeatures {

	public static final Map<ResourceLocation, ConfigDrivenFeatureEntry> ENTRIES = new HashMap<>();

	//

	public static final ConfigDrivenFeatureEntry ZINC_ORE =
		register("zinc_ore", 12, 8, BiomeTags.IS_OVERWORLD).between(-63, 70)
			.withBlocks(Couple.create(AllBlocks.ZINC_ORE, AllBlocks.DEEPSLATE_ZINC_ORE));

	public static final ConfigDrivenFeatureEntry STRIATED_ORES_OVERWORLD =
		register("striated_ores_overworld", 32, 1 / 12f, BiomeTags.IS_OVERWORLD).between(-30, 70)
			.withLayerPattern(AllLayerPatterns.SCORIA)
			.withLayerPattern(AllLayerPatterns.CINNABAR)
			.withLayerPattern(AllLayerPatterns.MAGNETITE)
			.withLayerPattern(AllLayerPatterns.MALACHITE)
			.withLayerPattern(AllLayerPatterns.LIMESTONE)
			.withLayerPattern(AllLayerPatterns.OCHRESTONE);

	public static final ConfigDrivenFeatureEntry STRIATED_ORES_NETHER =
		register("striated_ores_nether", 32, 1 / 12f, BiomeTags.IS_NETHER).between(40, 90)
			.withLayerPattern(AllLayerPatterns.SCORIA_NETHER)
			.withLayerPattern(AllLayerPatterns.SCORCHIA_NETHER);

	//

	private static ConfigDrivenFeatureEntry register(String id, int clusterSize, float frequency,
		TagKey<Biome> biomeTag) {
		ConfigDrivenFeatureEntry configDrivenFeatureEntry = new ConfigDrivenFeatureEntry(id, clusterSize, frequency);
		configDrivenFeatureEntry.biomeTag = biomeTag;
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
				ConfigDrivenFeatureEntry featureEntry = entry.getValue();
				featureEntry.configuredFeature = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, id,
					featureEntry.factory.apply(featureEntry));
				featureEntry.placedFeature =
					BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id, new PlacedFeature(
						featureEntry.configuredFeature, ImmutableList.of(new ConfigDrivenDecorator(featureEntry.id))));
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

	public static void registerOreFeatures(RegisterEvent event) {
		event.register(Registry.FEATURE_REGISTRY, helper -> {
			helper.register(Create.asResource("config_driven_ore"), VanillaStyleOreFeature.INSTANCE);
			helper.register(Create.asResource("config_driven_layered_ore"), LayeredOreFeature.INSTANCE);
		});
	}

	public static void registerPlacementTypes() {
		ConfigDrivenDecorator.TYPE =
			Registry.register(Registry.PLACEMENT_MODIFIERS, "create_config_driven", () -> ConfigDrivenDecorator.CODEC);
	}

	public static void generateBiomeModifiers(GatherDataEvent event) {
		Map<ResourceLocation, BiomeModifier> modifiers = new HashMap<>();
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

		for (Entry<ResourceLocation, ConfigDrivenFeatureEntry> entry : ENTRIES.entrySet()) {
			ConfigDrivenFeatureEntry feature = entry.getValue();
			HolderSet<Biome> biomes = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY)
				.get(), feature.biomeTag);
			modifiers.put(entry.getKey(), new AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature.placedFeature),
				Decoration.UNDERGROUND_ORES));
		}

		DataGenerator generator = event.getGenerator();
		generator.addProvider(event.includeServer(), JsonCodecProvider.forDatapackRegistry(generator,
			event.getExistingFileHelper(), Create.ID, ops, Keys.BIOME_MODIFIERS, modifiers));
	}

}
