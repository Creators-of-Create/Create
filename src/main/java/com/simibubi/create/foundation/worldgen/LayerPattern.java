package com.simibubi.create.foundation.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.worldgen.LayerPattern.Layer.LayerBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraftforge.common.util.NonNullConsumer;

public class LayerPattern {

	List<Layer> layers;

	public LayerPattern() {
		layers = new ArrayList<>();
	}

	public static Builder builder() {
		LayerPattern ore = new LayerPattern();
		return ore.new Builder();
	}

	public Layer rollNext(@Nullable Layer previous, Random random) {
		int totalWeight = 0;
		for (Layer layer : layers)
			if (layer != previous)
				totalWeight += layer.weight;
		int rolled = random.nextInt(totalWeight);

		for (Layer layer : layers) {
			if (layer == previous)
				continue;
			rolled -= layer.weight;
			if (rolled < 0)
				return layer;
		}
		return null;
	}

	class Builder {
		
		private boolean netherMode;

		public LayerPattern build() {
			return LayerPattern.this;
		}
		
		public Builder inNether() {
			netherMode = true;
			return this;
		}

		public Builder layer(NonNullConsumer<LayerBuilder> builder) {
			Layer layer = new Layer();
			LayerBuilder layerBuilder = layer.new LayerBuilder();
			layerBuilder.netherMode = netherMode;
			builder.accept(layerBuilder);
			layers.add(layerBuilder.build());
			return this;
		}

	}

	static class Layer {

		public List<List<TargetBlockState>> targets;
		public int minSize;
		public int maxSize;
		public int weight;

		public Layer() {
			this.targets = new ArrayList<>();
			this.minSize = 1;
			this.maxSize = 1;
			this.weight = 1;
		}

		public List<TargetBlockState> rollBlock(Random random) {
			if (targets.size() == 1)
				return targets.get(0);
			return targets.get(random.nextInt(targets.size()));
		}

		class LayerBuilder {

			private boolean netherMode;

			private Layer build() {
				return Layer.this;
			}

			public LayerBuilder block(NonNullSupplier<? extends Block> block) {
				return block(block.get());
			}

			public LayerBuilder passiveBlock() {
				return block(Blocks.AIR);
			}

			public LayerBuilder block(Block block) {
				if (netherMode) {
					Layer.this.targets.add(ImmutableList.of(OreConfiguration
						.target(OreFeatures.NETHER_ORE_REPLACEABLES, block.defaultBlockState())));
					return this;
				}
				return blocks(block.defaultBlockState(), block.defaultBlockState());
			}

			public LayerBuilder blocks(Couple<NonNullSupplier<? extends Block>> blocksByDepth) {
				return blocks(blocksByDepth.getFirst()
					.get()
					.defaultBlockState(),
					blocksByDepth.getSecond()
						.get()
						.defaultBlockState());
			}

			private LayerBuilder blocks(BlockState stone, BlockState deepslate) {
				Layer.this.targets.add(
					ImmutableList.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, stone),
						OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, deepslate)));
				return this;
			}

			public LayerBuilder weight(int weight) {
				Layer.this.weight = weight;
				return this;
			}

			public LayerBuilder size(int min, int max) {
				Layer.this.minSize = min;
				Layer.this.maxSize = max;
				return this;
			}

		}

	}

}
