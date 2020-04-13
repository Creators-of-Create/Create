package com.simibubi.create.foundation.world;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;

public class CountedOreFeature extends OreFeature<CountRangeConfig> {

	private ConfigInt clusterCount;

	public CountedOreFeature(NonNullSupplier<Block> block, int clusterSize, int clusterCount) {
		super(block, clusterSize);
		this.clusterCount = i(clusterCount, 0, "clusterCount");
	}

	@Override
	protected boolean canGenerate() {
		return super.canGenerate() && clusterCount.get() > 0;
	}

	@Override
	protected Pair<Placement<CountRangeConfig>, CountRangeConfig> getPlacement() {
		return Pair.of(Placement.COUNT_RANGE,
				new CountRangeConfig(clusterCount.get(), minHeight.get(), 0, maxHeight.get() - minHeight.get()));
	}

}
