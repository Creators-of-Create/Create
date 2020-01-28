package com.simibubi.create.foundation.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public enum OreGeneration {

	COPPER_ORE_GENERAL(new BasicOreGenConfig(AllBlocks.COPPER_ORE.get(), 21, 3, 40, 96)),
	COPPER_ORE_OCEAN(new OnlyInBiomes(new BasicOreGenConfig(AllBlocks.COPPER_ORE.get(), 15, 3, 20, 55), Biome.Category.OCEAN)),

	ZINC_ORE_GENERAL(new BasicOreGenConfig(AllBlocks.ZINC_ORE.get(), 17, 1, 55, 80)),
	ZINC_ORE_DESERT(new OnlyInBiomes(new BasicOreGenConfig(AllBlocks.ZINC_ORE.get(), 17, 5, 50, 85),Biome.Category.DESERT)),

	BASALT_ROCK(new BasicOreGenConfig(AllBlocks.VOLCANIC_ROCK.get(), 39, 0.2f, 0, 10)),

	//TEST_BLOB_1(new BasicOreGenConfig(Blocks.EMERALD_BLOCK, 25, 2, 0, 128)),
	//TEST_BLOB_2(new BasicOreGenConfig(Blocks.QUARTZ_BLOCK, 41, 3, 0, 25)),
	//TEST_BLOB_3(new OnlyInBiomes(new BasicOreGenConfig(Blocks.BLUE_GLAZED_TERRACOTTA, 5, 10, 0, 30), Biome.Category.OCEAN)),
	//TEST_BLOB_4(new OnlyInBiomes(new BasicOreGenConfig(AllBlocks.GABBRO.get(), 31, 2, 0, 128), Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.TAIGA_MOUNTAINS)),

	;

	IOreGenConfig config;

	OreGeneration(IOreGenConfig oreGenConfig) {
		this.config = oreGenConfig;
	}

	public static void setupOreGeneration() {

		for (Biome biome :
				ForgeRegistries.BIOMES) {

			Arrays.stream(values()).forEach(oreGen -> oreGen.config.addFeature(biome));
		}
	}

	private static class BasicOreGenConfig implements IOreGenConfig {

		Block block;
		//im not 100% certain that these names are accurate but at least they seem that way
		private int clusterSize, clusterCount, minHeight,  maxHeight;
		private float clusterChance;

		private BasicOreGenConfig(Block block, int clusterSize, int clusterCount, int minHeight, int maxHeight) {
			this.block = block;
			this.clusterSize = clusterSize;
			this.clusterCount = clusterCount;
			this.clusterChance = 1.0f;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
		}

		private BasicOreGenConfig(Block block, int clusterSize, float clusterChance, int minHeight, int maxHeight) {
			this.block = block;
			this.clusterSize = clusterSize;
			this.clusterCount = 0;
			this.clusterChance = clusterChance;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
		}

		@Override
		public void addFeature(Biome biome) {
			if (clusterCount > 0) {
				biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createDecoratedFeature(Feature.ORE,
								new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
										block.getDefaultState(), clusterSize),
								Placement.COUNT_RANGE, new CountRangeConfig(clusterCount, minHeight, 0, maxHeight)));
			} else {
				biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
						Biome.createDecoratedFeature(Feature.ORE,
								new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
										block.getDefaultState(), clusterSize),
								Placement.CHANCE_RANGE, new ChanceRangeConfig(clusterChance, minHeight, 0, maxHeight)));
			}
		}

	}

	private static class OnlyInBiomes implements IOreGenConfig {

		//not sure if this is really needed but w/e

		IOreGenConfig config;
		List<Biome> biomes;

		private OnlyInBiomes(IOreGenConfig config, Biome... biomes) {
			this.config = config;
			this.biomes = Arrays.asList(biomes);
		}

		private OnlyInBiomes(IOreGenConfig config, Biome.Category category){
			this.config = config;
			this.biomes = new LinkedList<>();
			for (Biome biome:
			     ForgeRegistries.BIOMES) {
				if (biome.getCategory() == category)
					biomes.add(biome);
			}
		}

		@Override
		public void addFeature(Biome biome) {
			if (biomes.contains(biome)) {
				config.addFeature(biome);
			}
		}
	}

	private interface IOreGenConfig {

		void addFeature(Biome biome);

	}
}
