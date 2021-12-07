package com.simibubi.create.foundation.worldgen;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ConfigDrivenDecorator extends PlacementModifier {

	public static PlacementModifierType<ConfigDrivenDecorator> TYPE;

	public static final Codec<ConfigDrivenDecorator> CODEC = RecordCodecBuilder.create((p_67849_) -> {
		return p_67849_.group(Codec.STRING.fieldOf("key")
			.forGetter(t -> t.key.getPath()))
			.apply(p_67849_, ConfigDrivenDecorator::new);
	});

	private ResourceLocation key;

	public ConfigDrivenDecorator(String key) {
		this.key = Create.asResource(key);
	}

	@Override
	public PlacementModifierType<?> type() {
		return TYPE;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, Random random, BlockPos pos) {
		ConfigDrivenOreConfiguration config = (ConfigDrivenOreConfiguration) entry().getFeature()
			.getFirst().config;
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

	protected ConfigDrivenFeatureEntry entry() {
		return AllWorldFeatures.ENTRIES.get(key);
	}

}
