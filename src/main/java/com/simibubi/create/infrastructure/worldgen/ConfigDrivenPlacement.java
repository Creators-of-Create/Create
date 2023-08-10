package com.simibubi.create.infrastructure.worldgen;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ConfigDrivenPlacement extends PlacementModifier {
	public static final Codec<ConfigDrivenPlacement> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			OreFeatureConfigEntry.CODEC
				.fieldOf("entry")
				.forGetter(ConfigDrivenPlacement::getEntry)
		).apply(instance, ConfigDrivenPlacement::new);
	});

	private final OreFeatureConfigEntry entry;

	public ConfigDrivenPlacement(OreFeatureConfigEntry entry) {
		this.entry = entry;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, Random random, BlockPos pos) {
		int count = getCount(getFrequency(), random);
		if (count == 0) {
			return Stream.empty();
		}

		int minY = getMinY();
		int maxY = getMaxY();

		return IntStream.range(0, count)
				.mapToObj(i -> pos)
				.map(p -> {
					int x = random.nextInt(16) + p.getX();
					int z = random.nextInt(16) + p.getZ();
					int y = Mth.randomBetweenInclusive(random, minY, maxY);
					return new BlockPos(x, y, z);
				});
	}

	public int getCount(float frequency, Random random) {
		int floored = Mth.floor(frequency);
		return floored + (random.nextFloat() < (frequency - floored) ? 1 : 0);
	}

	@Override
	public PlacementModifierType<?> type() {
		return AllPlacementModifiers.CONFIG_DRIVEN.get();
	}

	public OreFeatureConfigEntry getEntry() {
		return entry;
	}

	public float getFrequency() {
		if (AllConfigs.common().worldGen.disable.get())
			return 0;
		return entry.frequency.getF();
	}

	public int getMinY() {
		return entry.minHeight.get();
	}

	public int getMaxY() {
		return entry.maxHeight.get();
	}
}
