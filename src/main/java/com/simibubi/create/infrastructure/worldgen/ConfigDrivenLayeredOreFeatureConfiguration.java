package com.simibubi.create.infrastructure.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ConfigDrivenLayeredOreFeatureConfiguration extends BaseConfigDrivenOreFeatureConfiguration {
	public static final Codec<ConfigDrivenLayeredOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			OreFeatureConfigEntry.CODEC
				.fieldOf("entry")
				.forGetter(config -> config.entry),
			Codec.floatRange(0.0F, 1.0F)
				.fieldOf("discard_chance_on_air_exposure")
				.forGetter(config -> config.discardChanceOnAirExposure),
			Codec.list(LayerPattern.CODEC)
				.fieldOf("layer_patterns")
				.forGetter(config -> config.layerPatterns)
		).apply(instance, ConfigDrivenLayeredOreFeatureConfiguration::new);
	});

	private final List<LayerPattern> layerPatterns;

	public ConfigDrivenLayeredOreFeatureConfiguration(OreFeatureConfigEntry entry, float discardChance, List<LayerPattern> layerPatterns) {
		super(entry, discardChance);
		this.layerPatterns = layerPatterns;
	}

	public List<LayerPattern> getLayerPatterns() {
		return layerPatterns;
	}
}
