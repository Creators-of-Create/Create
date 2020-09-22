package com.simibubi.create.foundation.worldgen;

import static net.minecraft.world.biome.Biome.Category.DESERT;
import static net.minecraft.world.biome.Biome.Category.OCEAN;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public enum AllWorldFeatures {

	COPPER_ORE(new CountedOreFeature(AllBlocks.COPPER_ORE, 18, 2).between(40, 86)),
	COPPER_ORE_OCEAN(new CountedOreFeature(AllBlocks.COPPER_ORE, 15, 4).between(20, 55).inBiomes(OCEAN)),

	ZINC_ORE(new CountedOreFeature(AllBlocks.ZINC_ORE, 14, 4).between(15, 70)),
	ZINC_ORE_DESERT(new CountedOreFeature(AllBlocks.ZINC_ORE, 17, 5).between(10, 85).inBiomes(DESERT)),

	LIMESTONE(new ChanceOreFeature(AllPaletteBlocks.LIMESTONE, 128, 1 / 32f).between(30, 70)),
	WEATHERED_LIMESTONE(new ChanceOreFeature(AllPaletteBlocks.WEATHERED_LIMESTONE, 128, 1 / 32f).between(10, 30)),
	DOLOMITE(new ChanceOreFeature(AllPaletteBlocks.DOLOMITE, 128, 1 / 64f).between(20, 70)),
	GABBRO(new ChanceOreFeature(AllPaletteBlocks.GABBRO, 128, 1 / 64f).between(20, 70)),
	SCORIA(new ChanceOreFeature(AllPaletteBlocks.NATURAL_SCORIA, 128, 1 / 32f).between(0, 10)), 

	;

	/**
	 * Increment this number if all worldgen entries should be overwritten in this
	 * update. Worlds from the previous version will overwrite potentially changed
	 * values with the new defaults.
	 */
	public static final int forcedUpdateVersion = 1;

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

				if (biome.getRegistryName() == Biomes.THE_VOID.getRegistryName())
					continue;
				if (biome == Biomes.NETHER)
					continue;

				if (entry.featureInstances.containsKey(biome))
					biome.getFeatures(entry.feature.getGenerationStage()).remove(entry.featureInstances.remove(biome));
				Optional<ConfiguredFeature<?, ?>> createFeature = entry.feature.createFeature(biome);
				if (!createFeature.isPresent())
					continue;

				entry.featureInstances.put(biome, createFeature.get());
				biome.addFeature(entry.feature.getGenerationStage(), createFeature.get());
			}
		}

//		// Debug contained ore features
//		for (Biome biome : ForgeRegistries.BIOMES) {
//			Debug.markTemporary();
//			System.out.println(biome.getRegistryName().getPath() + " has the following features:");
//			for (ConfiguredFeature<?> configuredFeature : biome.getFeatures(Decoration.UNDERGROUND_ORES)) {
//				IFeatureConfig config = configuredFeature.config;
//				if (!(config instanceof DecoratedFeatureConfig))
//					continue;
//				DecoratedFeatureConfig decoConf = (DecoratedFeatureConfig) config;
//				if (!(decoConf.feature.config instanceof OreFeatureConfig))
//					continue;
//				OreFeatureConfig oreConf = (OreFeatureConfig) decoConf.feature.config;
//				System.out.println(configuredFeature.feature.getRegistryName().getPath());
//				System.out.println(oreConf.state.getBlock().getRegistryName().getPath());
//				System.out.println("--");
//			}
//		}
	}

	public static void fillConfig(ForgeConfigSpec.Builder builder) {
		Arrays.stream(values()).forEach(entry -> {
			builder.push(Lang.asId(entry.name()));
			entry.feature.addToConfig(builder);
			builder.pop();
		});
	}

}
