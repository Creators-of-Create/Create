package com.simibubi.create.infrastructure.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

public class ConfigDrivenOreFeatureConfiguration extends BaseConfigDrivenOreFeatureConfiguration {
	public static final Codec<ConfigDrivenOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			OreFeatureConfigEntry.CODEC
				.fieldOf("entry")
				.forGetter(config -> config.entry),
			Codec.floatRange(0.0F, 1.0F)
				.fieldOf("discard_chance_on_air_exposure")
				.forGetter(config -> config.discardChanceOnAirExposure),
			Codec.list(TargetBlockState.CODEC)
				.fieldOf("targets")
				.forGetter(config -> config.targetStates)
		).apply(instance, ConfigDrivenOreFeatureConfiguration::new);
	});

	private final List<TargetBlockState> targetStates;

	public ConfigDrivenOreFeatureConfiguration(OreFeatureConfigEntry entry, float discardChance, List<TargetBlockState> targetStates) {
		super(entry, discardChance);
		this.targetStates = targetStates;
	}

	public List<TargetBlockState> getTargetStates() {
		return targetStates;
	}
}
