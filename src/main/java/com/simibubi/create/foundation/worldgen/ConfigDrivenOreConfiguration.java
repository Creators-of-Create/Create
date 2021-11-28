package com.simibubi.create.foundation.worldgen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

public class ConfigDrivenOreConfiguration implements FeatureConfiguration, DecoratorConfiguration {

	public static final Codec<ConfigDrivenOreConfiguration> CODEC = RecordCodecBuilder.create((p_67849_) -> {
		return p_67849_.group(Codec.list(TargetBlockState.CODEC)
			.fieldOf("targets")
			.forGetter((p_161027_) -> {
				return p_161027_.targetStates;
			}), Codec.floatRange(0.0F, 1.0F)
				.fieldOf("discard_chance_on_air_exposure")
				.forGetter((p_161020_) -> {
					return p_161020_.discardChanceOnAirExposure;
				}),
			Codec.STRING.fieldOf("key")
				.forGetter(t -> t.key.getPath()))
			.apply(p_67849_, ConfigDrivenOreConfiguration::new);
	});

	public final List<TargetBlockState> targetStates;
	public final float discardChanceOnAirExposure;
	public final ResourceLocation key;

	public ConfigDrivenOreConfiguration(List<TargetBlockState> targetStates, float discardChance, String key) {
		this.targetStates = targetStates;
		this.discardChanceOnAirExposure = discardChance;
		this.key = Create.asResource(key);
	}

	public int getSize() {
		return entry().clusterSize.get();
	}

	public int getMinY() {
		return entry().minHeight.get();
	}

	public int getMaxY() {
		return entry().maxHeight.get();
	}

	public List<LayerPattern> getLayers() {
		return LayeredOreFeature.LAYER_PATTERNS.get(key);
	}

	public float getFrequency() {
		if (AllConfigs.COMMON.worldGen.disable.get())
			return 0;
		return entry().frequency.getF();
	}

	protected ConfigDrivenFeatureEntry entry() {
		return AllWorldFeatures.ENTRIES.get(key);
	}

}
