package com.simibubi.create.foundation.block;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashBiMap;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class CopperRegistries {
	private static final Map<Holder<Block>, Holder<Block>> WEATHERING = HashBiMap.create();
	private static final Map<Holder<Block>, Holder<Block>> WAXABLE = HashBiMap.create();

	public static Map<Holder<Block>, Holder<Block>> getWeatheringView() {
		return Collections.unmodifiableMap(WEATHERING);
	}

	public static Map<Holder<Block>, Holder<Block>> getWaxableView() {
		return Collections.unmodifiableMap(WAXABLE);
	}

	public static synchronized void addWeathering(Holder<Block> original, Holder<Block> weathered) {
		WEATHERING.put(original, weathered);
	}

	public static synchronized void addWaxable(Holder<Block> original, Holder<Block> waxed) {
		WAXABLE.put(original, waxed);
	}
}
