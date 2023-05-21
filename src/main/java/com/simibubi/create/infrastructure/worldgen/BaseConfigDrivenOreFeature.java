package com.simibubi.create.infrastructure.worldgen;

import java.util.Random;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public abstract class BaseConfigDrivenOreFeature<FC extends BaseConfigDrivenOreFeatureConfiguration> extends Feature<FC> {
	public BaseConfigDrivenOreFeature(Codec<FC> configCodec) {
		super(configCodec);
	}

	public boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
		Random pRandom, BaseConfigDrivenOreFeatureConfiguration pConfig, OreConfiguration.TargetBlockState pTargetState,
		BlockPos.MutableBlockPos pMatablePos) {
		if (!pTargetState.target.test(pState, pRandom))
			return false;
		if (shouldSkipAirCheck(pRandom, pConfig.getDiscardChanceOnAirExposure()))
			return true;

		return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
	}

	protected boolean shouldSkipAirCheck(Random pRandom, float pChance) {
		return pChance <= 0 ? true : pChance >= 1 ? false : pRandom.nextFloat() >= pChance;
	}
}
