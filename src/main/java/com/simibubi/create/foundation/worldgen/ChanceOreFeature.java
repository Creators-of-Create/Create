package com.simibubi.create.foundation.worldgen;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

public class ChanceOreFeature extends OreFeature<ChanceConfig> {

	private ConfigFloat clusterChance;

	public ChanceOreFeature(NonNullSupplier<? extends Block> block, int clusterSize, float clusterChance) {
		super(block, clusterSize);
		this.clusterChance = f(clusterChance, 0, 1, "clusterChance");
	}

	@Override
	protected boolean canGenerate() {
		return super.canGenerate() && clusterChance.get() > 0;
	}

	@Override
	protected Pair<Placement<ChanceConfig>, ChanceConfig> getPlacement() {
		return Pair.of(Placement.CHANCE,
			// TODO 1.16 worldgen verify this
			new ChanceConfig((int) (1 / clusterChance.getF())));
	}
}
