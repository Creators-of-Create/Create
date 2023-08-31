package com.simibubi.create.infrastructure.worldgen;

import com.mojang.serialization.Codec;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ConfigPlacementFilter extends PlacementFilter {
	public static final ConfigPlacementFilter INSTANCE = new ConfigPlacementFilter();
	public static final Codec<ConfigPlacementFilter> CODEC = Codec.unit(() -> INSTANCE);

	@Override
	protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
		return !AllConfigs.common().worldGen.disable.get();
	}

	@Override
	public PlacementModifierType<?> type() {
		return AllPlacementModifiers.CONFIG_FILTER.get();
	}
}
