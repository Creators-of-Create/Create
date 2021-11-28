package com.simibubi.create.foundation.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.utility.Couple;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigDrivenFeatureEntry extends ConfigBase {

	public final String id;
	public BiomeFilter biomeFilter;

	private NonNullSupplier<? extends Block> block;
	private NonNullSupplier<? extends Block> deepblock;
	private NonNullSupplier<? extends Block> netherblock;

	private List<NonNullSupplier<LayerPattern>> layers = new ArrayList<>();

	protected ConfigInt clusterSize;
	protected ConfigInt minHeight;
	protected ConfigInt maxHeight;
	protected ConfigFloat frequency;

	Function<ConfigDrivenFeatureEntry, ? extends ConfiguredFeature<?, ?>> factory;
	Optional<ConfiguredFeature<?, ?>> feature = Optional.empty();

	public ConfigDrivenFeatureEntry(String id, int clusterSize, float frequency) {
		this.id = id;
		this.factory = this::standardFactory;
		this.clusterSize = i(clusterSize, 0, "clusterSize");
		this.minHeight = i(0, 0, "minHeight");
		this.maxHeight = i(256, 0, "maxHeight");
		this.frequency = f(frequency, 0, 512, "frequency", "Amount of clusters generated per Chunk.",
			"  >1 to spawn multiple.", "  <1 to make it a chance.", "  0 to disable.");
	}

	public ConfigDrivenFeatureEntry withLayerPattern(NonNullSupplier<LayerPattern> pattern) {
		this.layers.add(pattern);
		this.factory = this::layersFactory;
		return this;
	}

	public ConfigDrivenFeatureEntry withBlock(NonNullSupplier<? extends Block> block) {
		this.block = this.deepblock = block;
		return this;
	}

	public ConfigDrivenFeatureEntry withNetherBlock(NonNullSupplier<? extends Block> block) {
		this.netherblock = block;
		return this;
	}

	public ConfigDrivenFeatureEntry withBlocks(Couple<NonNullSupplier<? extends Block>> blocks) {
		this.block = blocks.getFirst();
		this.deepblock = blocks.getSecond();
		return this;
	}

	public ConfigDrivenFeatureEntry between(int minHeight, int maxHeight) {
		allValues.remove(this.minHeight);
		allValues.remove(this.maxHeight);
		this.minHeight = i(minHeight, 0, "minHeight");
		this.maxHeight = i(maxHeight, 0, "maxHeight");
		return this;
	}

	public ConfiguredFeature<?, ?> getFeature() {
		if (!feature.isPresent())
			feature = Optional.of(factory.apply(this));
		return feature.get();
	}

	private ConfiguredFeature<?, ?> layersFactory(ConfigDrivenFeatureEntry entry) {
		ConfigDrivenOreConfiguration config = new ConfigDrivenOreConfiguration(ImmutableList.of(), 0, id);
		LayeredOreFeature.LAYER_PATTERNS.put(Create.asResource(id), layers.stream()
			.map(NonNullSupplier::get)
			.toList());
		return LayeredOreFeature.INSTANCE.configured(config)
			.decorated(ConfigDrivenDecorator.INSTANCE.configured(config));
	}

	private ConfiguredFeature<?, ?> standardFactory(ConfigDrivenFeatureEntry entry) {
		ConfigDrivenOreConfiguration config = new ConfigDrivenOreConfiguration(createTarget(), 0, id);
		return VanillaStyleOreFeature.INSTANCE.configured(config)
			.decorated(ConfigDrivenDecorator.INSTANCE.configured(config));
	}

	private List<TargetBlockState> createTarget() {
		List<TargetBlockState> list = new ArrayList<>();
		if (block != null)
			list.add(OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, block.get()
				.defaultBlockState()));
		if (deepblock != null)
			list.add(OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, deepblock.get()
				.defaultBlockState()));
		if (netherblock != null)
			list.add(OreConfiguration.target(OreConfiguration.Predicates.NETHER_ORE_REPLACEABLES, netherblock.get()
				.defaultBlockState()));
		return list;
	}

	public void addToConfig(ForgeConfigSpec.Builder builder) {
		registerAll(builder);
	}

	@Override
	public String getName() {
		return id;
	}

}
