package com.simibubi.create.foundation.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ConfigDrivenOreFeatureConfig implements FeatureConfiguration, DecoratorConfiguration {

	public static final Codec<ConfigDrivenOreFeatureConfig> CODEC = RecordCodecBuilder.create((p_236568_0_) -> {
		return p_236568_0_.group(RuleTest.CODEC.fieldOf("target")
			.forGetter((p_236570_0_) -> {
				return p_236570_0_.target;
			}), BlockState.CODEC.fieldOf("state")
				.forGetter((p_236569_0_) -> {
					return p_236569_0_.state;
				}),
			Codec.STRING.fieldOf("key")
				.forGetter(t -> t.key))
			.apply(p_236568_0_, ConfigDrivenOreFeatureConfig::new);
	});

	public final RuleTest target;
	public final BlockState state;
	public final String key;

	public ConfigDrivenOreFeatureConfig(RuleTest target, BlockState state, String key) {
		this.target = target;
		this.state = state;
		this.key = key;
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

	public float getFrequency() {
		if (AllConfigs.COMMON.worldGen.disable.get())
			return 0;
		return entry().frequency.getF();
	}

	protected ConfigDrivenFeatureEntry entry() {
		return AllWorldFeatures.entries.get(key);
	}

}
