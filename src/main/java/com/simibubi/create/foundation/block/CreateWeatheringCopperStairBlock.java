package com.simibubi.create.foundation.block;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;

public class CreateWeatheringCopperStairBlock extends WeatheringCopperStairBlock {
	public static final MapCodec<WeatheringCopperStairBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge),
			propertiesCodec()
	).apply(i, CreateWeatheringCopperStairBlock::new));

	public CreateWeatheringCopperStairBlock(WeatherState weatherState, Properties properties) {
		super(weatherState, Blocks.AIR.defaultBlockState(), properties);
	}

	@Override
	public float getExplosionResistance() {
		return explosionResistance;
	}

	@Override
	public @NotNull MapCodec<WeatheringCopperStairBlock> codec() {
		return CODEC;
	}
}
