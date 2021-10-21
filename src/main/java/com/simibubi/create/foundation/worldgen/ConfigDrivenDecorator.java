package com.simibubi.create.foundation.worldgen;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.placement.SimplePlacement;

public class ConfigDrivenDecorator extends SimplePlacement<ConfigDrivenOreFeatureConfig> {

	public static final ConfigDrivenDecorator INSTANCE = new ConfigDrivenDecorator();

	public ConfigDrivenDecorator() {
		super(ConfigDrivenOreFeatureConfig.CODEC);
		setRegistryName("create_config_driven_decorator");
	}

	@Override
	protected Stream<BlockPos> place(Random r, ConfigDrivenOreFeatureConfig config, BlockPos pos) {
		float frequency = config.getFrequency();

		int floored = MathHelper.floor(frequency);
		int count = floored + (r.nextFloat() < frequency - floored ? 1 : 0);
		if (count == 0)
			return Stream.empty();

		int maxY = config.getMaxY();
		int minY = config.getMinY();

		return IntStream.range(0, count)
			.mapToObj($ -> pos)
			.map(p -> {
				int i = p.getX();
				int j = p.getZ();
				int k = r.nextInt(maxY - minY) + minY;
				return new BlockPos(i, k, j);
			});
	}

}
