package com.simibubi.create.api.data.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#FURNACE_FUELS furnace fuels}.
 *
 * @param burnTime how long (in ticks) the item will burn for
 */
public record BlazeBurnerFuel(int burnTime) {
	public static final Codec<BlazeBurnerFuel> BURN_TIME_CODEC = ExtraCodecs.POSITIVE_INT
		.xmap(BlazeBurnerFuel::new, BlazeBurnerFuel::burnTime);
	public static final Codec<BlazeBurnerFuel> CODEC = Codec.withAlternative(
		RecordCodecBuilder.create(i -> i.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("burn_time").forGetter(BlazeBurnerFuel::burnTime)
		).apply(i, BlazeBurnerFuel::new)),
		BURN_TIME_CODEC
	);
}
