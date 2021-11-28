package com.simibubi.create.foundation.worldgen;

import java.util.Random;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public abstract class OreFeatureBase extends Feature<ConfigDrivenOreConfiguration> {

	public OreFeatureBase() {
		super(ConfigDrivenOreConfiguration.CODEC);
	}

	public boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
		Random pRandom, ConfigDrivenOreConfiguration pConfig, OreConfiguration.TargetBlockState pTargetState,
		BlockPos.MutableBlockPos pMatablePos) {
		if (!pTargetState.target.test(pState, pRandom))
			return false;
		if (shouldSkipAirCheck(pRandom, pConfig.discardChanceOnAirExposure))
			return true;

		return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
	}

	protected boolean shouldSkipAirCheck(Random pRandom, float pChance) {
		return pChance <= 0 ? true : pChance >= 1 ? false : pRandom.nextFloat() >= pChance;
	}
}
