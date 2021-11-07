package com.simibubi.create.foundation.worldgen;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class ConfigDrivenDecorator extends FeatureDecorator<ConfigDrivenOreFeatureConfig> {

	public static final ConfigDrivenDecorator INSTANCE = new ConfigDrivenDecorator();

	public ConfigDrivenDecorator() {
		super(ConfigDrivenOreFeatureConfig.CODEC);
		setRegistryName("config_driven_decorator");
	}

	@Override
	public Stream<BlockPos> getPositions(DecorationContext context, Random random, ConfigDrivenOreFeatureConfig config, BlockPos pos) {
		float frequency = config.getFrequency();

		int floored = Mth.floor(frequency);
		int count = floored + (random.nextFloat() < frequency - floored ? 1 : 0);
		if (count == 0)
			return Stream.empty();

		int maxY = config.getMaxY();
		int minY = config.getMinY();

		return IntStream.range(0, count)
			.mapToObj($ -> pos)
			.map(p -> {
				int i = p.getX();
				int j = p.getZ();
				int k = random.nextInt(maxY - minY) + minY;
				return new BlockPos(i, k, j);
			});
	}

}
