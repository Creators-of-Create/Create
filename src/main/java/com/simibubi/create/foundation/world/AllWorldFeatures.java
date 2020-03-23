package com.simibubi.create.foundation.world;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public enum AllWorldFeatures {

	COPPER_ORE(new CountedOreFeature(AllBlocks.COPPER_ORE.get(), 21, 1).between(40, 96)),
	COPPER_ORE_OCEAN(
			new CountedOreFeature(AllBlocks.COPPER_ORE.get(), 15, 4).between(20, 55).inBiomes(Biome.Category.OCEAN)),

	ZINC_ORE(new CountedOreFeature(AllBlocks.ZINC_ORE.get(), 17, 1).between(55, 80)),
	ZINC_ORE_DESERT(
			new CountedOreFeature(AllBlocks.ZINC_ORE.get(), 17, 5).between(50, 85).inBiomes(Biome.Category.DESERT)),

	LIMESTONE(new ChanceOreFeature(AllBlocks.LIMESTONE.get(), 128, 1 / 32f).between(30, 70)),
	WEATHERED_LIMESTONE(new ChanceOreFeature(AllBlocks.WEATHERED_LIMESTONE.get(), 128, 1 / 32f).between(10, 30)),
	DOLOMITE(new ChanceOreFeature(AllBlocks.DOLOMITE.get(), 128, 1 / 64f).between(20, 70)),
	GABBRO(new ChanceOreFeature(AllBlocks.GABBRO.get(), 128, 1 / 64f).between(20, 70)),
	SCORIA(new ChanceOreFeature(AllBlocks.NATURAL_SCORIA.get(), 128, 1 / 32f).between(0, 10)),

	;

	public IFeature feature;
	private Map<Biome, ConfiguredFeature<?, ?>> featureInstances;

	AllWorldFeatures(IFeature feature) {
		this.feature = feature;
		this.featureInstances = new HashMap<>();
		this.feature.setId(Lang.asId(name()));
	}

	public static void reload() {
		for (AllWorldFeatures entry : AllWorldFeatures.values()) {
			for (Biome biome : ForgeRegistries.BIOMES) {

				if (entry.featureInstances.containsKey(biome))
					biome.getFeatures(entry.feature.getGenerationStage()).remove(entry.featureInstances.remove(biome));

				Optional<ConfiguredFeature<?, ?>> createFeature = entry.feature.createFeature(biome);
				if (!createFeature.isPresent())
					continue;

				entry.featureInstances.put(biome, createFeature.get());
				biome.addFeature(entry.feature.getGenerationStage(), createFeature.get());
			}
		}
	}

	public static void fillConfig(ForgeConfigSpec.Builder builder) {
		Arrays.stream(values()).forEach(entry -> {
			builder.push(Lang.asId(entry.name()));
			entry.feature.addToConfig(builder);
			builder.pop();
		});
	}

}
