package com.simibubi.create.foundation.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public enum OreGeneration {

	TEST_BLOB_1(new BasicOreGenConfig(Blocks.EMERALD_BLOCK, 25, 2, 128)),
	TEST_BLOB_2(new BasicOreGenConfig(Blocks.QUARTZ_BLOCK, 41, 3, 25)),
	TEST_BLOB_3(new OnlyInBiomes(new BasicOreGenConfig(Blocks.BLUE_GLAZED_TERRACOTTA, 5, 10, 30), Biome.Category.OCEAN)),
	TEST_BLOB_4(new OnlyInBiomes(new BasicOreGenConfig(AllBlocks.GABBRO.get(), 31, 2, 128), Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.TAIGA_MOUNTAINS)),

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
		int clusterSize, clusterCount/*per chunk*/, maxHeight;

		private BasicOreGenConfig(Block block, int clusterSize, int clusterCount, int maxHeight) {
			this.block = block;
			this.clusterSize = clusterSize;
			this.clusterCount = clusterCount;
			this.maxHeight = maxHeight;
		}

		@Override
		public void addFeature(Biome biome) {
			biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
					Biome.createDecoratedFeature(Feature.ORE,
							new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
									block.getDefaultState(), clusterSize),
							Placement.COUNT_RANGE, new CountRangeConfig(clusterCount, 0, 0, maxHeight)));
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
