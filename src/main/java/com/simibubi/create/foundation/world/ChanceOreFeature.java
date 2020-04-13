package com.simibubi.create.foundation.world;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.Placement;

public class ChanceOreFeature extends OreFeature<ChanceRangeConfig> {

	private ConfigFloat clusterChance;

	public ChanceOreFeature(NonNullSupplier<Block> block, int clusterSize, float clusterChance) {
		super(block, clusterSize);
		this.clusterChance = f(clusterChance, 0, 1, "clusterChance");
	}

	@Override
	protected boolean canGenerate() {
		return super.canGenerate() && clusterChance.get() > 0;
	}

	@Override
	protected Pair<Placement<ChanceRangeConfig>, ChanceRangeConfig> getPlacement() {
		return Pair.of(Placement.CHANCE_RANGE,
				new ChanceRangeConfig(clusterChance.getF(), minHeight.get(), 0, maxHeight.get() - minHeight.get()));
	}

}
