package com.simibubi.create.foundation.block;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;

public class CreateCopperStairBlock extends StairBlock {
	public static final MapCodec<StairBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			propertiesCodec()
	).apply(i, CreateCopperStairBlock::new));

	public CreateCopperStairBlock(Properties properties) {
		super(Blocks.AIR.defaultBlockState(), properties);
	}

	@Override
	public float getExplosionResistance() {
		return explosionResistance;
	}

	@Override
	public @NotNull MapCodec<? extends StairBlock> codec() {
		return CODEC;
	}
}
