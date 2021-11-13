package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;

public interface BiomeLoadingCallback {
	public static final Event<BiomeLoadingCallback> EVENT = EventFactory.createArrayBacked(BiomeLoadingCallback.class, callbacks -> (key, category, generation) -> {
		for (BiomeLoadingCallback callback : callbacks) {
			generation = callback.onBiomeLoad(key, category, generation);
		}
		return generation;
	});

	BiomeGenerationSettings.Builder onBiomeLoad(ResourceLocation key, Biome.BiomeCategory category, BiomeGenerationSettings.Builder generation);
}
