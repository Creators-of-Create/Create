package com.simibubi.create.infrastructure.worldgen;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BaseConfigDrivenOreFeatureConfiguration implements FeatureConfiguration {
	protected final OreFeatureConfigEntry entry;
	protected final float discardChanceOnAirExposure;

	public BaseConfigDrivenOreFeatureConfiguration(OreFeatureConfigEntry entry, float discardChance) {
		this.entry = entry;
		this.discardChanceOnAirExposure = discardChance;
	}

	public OreFeatureConfigEntry getEntry() {
		return entry;
	}

	public int getClusterSize() {
		return entry.clusterSize.get();
	}

	public float getDiscardChanceOnAirExposure() {
		return discardChanceOnAirExposure;
	}
}
