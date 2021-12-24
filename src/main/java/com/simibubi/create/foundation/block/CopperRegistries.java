package com.simibubi.create.foundation.block;

import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.world.level.block.Block;

public class CopperRegistries {
	private static final BiMap<Supplier<Block>, Supplier<Block>> WEATHERING = HashBiMap.create();
	private static final BiMap<Supplier<Block>, Supplier<Block>> WAXABLE = HashBiMap.create();

	private static boolean injected;
	private static boolean weatheringMemoized;
	private static boolean waxableMemoized;

	public static synchronized void addWeathering(Supplier<Block> original, Supplier<Block> weathered) {
		if (weatheringMemoized) {
			throw new IllegalStateException("Cannot add weathering entry to CopperRegistries after memoization!");
		}
		WEATHERING.put(original, weathered);
	}

	public static synchronized void addWaxable(Supplier<Block> original, Supplier<Block> waxed) {
		if (waxableMemoized) {
			throw new IllegalStateException("Cannot add waxable entry to CopperRegistries after memoization!");
		}
		WAXABLE.put(original, waxed);
	}

	public static void inject() {
		if (injected) {
			throw new IllegalStateException("Cannot inject CopperRegistries twice!");
		}
		injected = true;

//		try {
//			Field delegateField = WeatheringCopper.NEXT_BY_BLOCK.getClass().getDeclaredField("delegate");
//			delegateField.setAccessible(true);
//			// Get the original delegate to prevent an infinite loop
//			@SuppressWarnings("unchecked")
//			Supplier<BiMap<Block, Block>> originalWeatheringMapDelegate = (Supplier<BiMap<Block, Block>>) delegateField.get(WeatheringCopper.NEXT_BY_BLOCK);
//			com.google.common.base.Supplier<BiMap<Block, Block>> weatheringMapDelegate = () -> {
//				weatheringMemoized = true;
//				ImmutableBiMap.Builder<Block, Block> builder = ImmutableBiMap.builder();
//				builder.putAll(originalWeatheringMapDelegate.get());
//				WEATHERING.forEach((original, weathered) -> {
//					builder.put(original.get(), weathered.get());
//				});
//				return builder.build();
//			};
//			// Replace the memoized supplier's delegate, since interface fields cannot be reassigned
//			delegateField.set(WeatheringCopper.NEXT_BY_BLOCK, weatheringMapDelegate);
//		} catch (Exception e) {
//			Create.LOGGER.error("Failed to inject weathering copper from CopperRegistries", e);
//		}
//
//		Supplier<BiMap<Block, Block>> originalWaxableMapSupplier = HoneycombItem.WAXABLES;
//		Supplier<BiMap<Block, Block>> waxableMapSupplier = Suppliers.memoize(() -> {
//			waxableMemoized = true;
//			ImmutableBiMap.Builder<Block, Block> builder = ImmutableBiMap.builder();
//			builder.putAll(originalWaxableMapSupplier.get());
//			WAXABLE.forEach((original, waxed) -> {
//				builder.put(original.get(), waxed.get());
//			});
//			return builder.build();
//		});
//		HoneycombItemAccessor.setWAXABLES(waxableMapSupplier);

		WEATHERING.forEach((original, weathered) -> OxidizableBlocksRegistry.registerOxidizableBlockPair(original.get(), weathered.get()));
		weatheringMemoized = true;
		WAXABLE.forEach((original, waxed) -> OxidizableBlocksRegistry.registerWaxableBlockPair(original.get(), waxed.get()));
		waxableMemoized = true;
	}
}
