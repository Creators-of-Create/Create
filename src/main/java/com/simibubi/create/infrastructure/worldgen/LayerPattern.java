package com.simibubi.create.infrastructure.worldgen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.createmod.catnip.utility.Couple;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraftforge.common.util.NonNullConsumer;

public class LayerPattern {
	public static final Codec<LayerPattern> CODEC = Codec.list(Layer.CODEC)
			.xmap(LayerPattern::new, pattern -> pattern.layers);

	public final List<Layer> layers;

	public LayerPattern(List<Layer> layers) {
		this.layers = layers;
	}

	public Layer rollNext(@Nullable Layer previous, RandomSource random) {
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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Layer> layers = new ArrayList<>();
		private boolean netherMode;

		public Builder inNether() {
			netherMode = true;
			return this;
		}

		public Builder layer(NonNullConsumer<Layer.Builder> builder) {
			Layer.Builder layerBuilder = new Layer.Builder();
			layerBuilder.netherMode = netherMode;
			builder.accept(layerBuilder);
			layers.add(layerBuilder.build());
			return this;
		}

		public LayerPattern build() {
			return new LayerPattern(layers);
		}
	}

	public static class Layer {
		public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
				Codec.list(Codec.list(TargetBlockState.CODEC))
					.fieldOf("targets")
					.forGetter(layer -> layer.targets),
				Codec.intRange(0, Integer.MAX_VALUE)
					.fieldOf("min_size")
					.forGetter(layer -> layer.minSize),
				Codec.intRange(0, Integer.MAX_VALUE)
					.fieldOf("max_size")
					.forGetter(layer -> layer.maxSize),
				Codec.intRange(0, Integer.MAX_VALUE)
					.fieldOf("weight")
					.forGetter(layer -> layer.weight)
			).apply(instance, Layer::new);
		});

		public final List<List<TargetBlockState>> targets;
		public final int minSize;
		public final int maxSize;
		public final int weight;

		public Layer(List<List<TargetBlockState>> targets, int minSize, int maxSize, int weight) {
			this.targets = targets;
			this.minSize = minSize;
			this.maxSize = maxSize;
			this.weight = weight;
		}

		public List<TargetBlockState> rollBlock(RandomSource random) {
			if (targets.size() == 1)
				return targets.get(0);
			return targets.get(random.nextInt(targets.size()));
		}

		public static class Builder {
			private final List<List<TargetBlockState>> targets = new ArrayList<>();
			private int minSize = 1;
			private int maxSize = 1;
			private int weight = 1;
			private boolean netherMode;

			public Builder block(NonNullSupplier<? extends Block> block) {
				return block(block.get());
			}

			public Builder passiveBlock() {
				return blocks(Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState());
			}

			public Builder block(Block block) {
				if (netherMode) {
					this.targets.add(ImmutableList.of(OreConfiguration
						.target(OreFeatures.NETHER_ORE_REPLACEABLES, block.defaultBlockState())));
					return this;
				}
				return blocks(block.defaultBlockState(), block.defaultBlockState());
			}

			public Builder blocks(Block block, Block deepblock) {
				return blocks(block.defaultBlockState(), deepblock.defaultBlockState());
			}

			public Builder blocks(Couple<NonNullSupplier<? extends Block>> blocksByDepth) {
				return blocks(blocksByDepth.getFirst()
					.get()
					.defaultBlockState(),
					blocksByDepth.getSecond()
						.get()
						.defaultBlockState());
			}

			private Builder blocks(BlockState stone, BlockState deepslate) {
				this.targets.add(
					ImmutableList.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, stone),
						OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, deepslate)));
				return this;
			}

			public Builder weight(int weight) {
				this.weight = weight;
				return this;
			}

			public Builder size(int min, int max) {
				this.minSize = min;
				this.maxSize = max;
				return this;
			}

			public Layer build() {
				return new Layer(targets, minSize, maxSize, weight);
			}
		}
	}
}
