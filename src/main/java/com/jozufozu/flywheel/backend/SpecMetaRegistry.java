package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.util.ResourceLocation;

public class SpecMetaRegistry {

	private static final Map<ResourceLocation, IProgramExtension> registeredExtensions = new HashMap<>();
	private static final Map<ResourceLocation, IGameStateProvider> registeredStateProviders = new HashMap<>();

	static void clear() {
		registeredExtensions.clear();
		registeredStateProviders.clear();
	}

	public static IGameStateProvider getStateProvider(ResourceLocation location) {
		IGameStateProvider out = registeredStateProviders.get(location);

		if (out == null) {
			throw new IllegalArgumentException("State provider '" + location + "' does not exist.");
		}

		return out;
	}

	public static IProgramExtension getExtension(ResourceLocation location) {
		IProgramExtension out = registeredExtensions.get(location);

		if (out == null) {
			throw new IllegalArgumentException("Extension '" + location + "' does not exist.");
		}

		return out;
	}

	public static void register(IGameStateProvider context) {
		if (registeredStateProviders.containsKey(context.getID())) {
			throw new IllegalStateException("Duplicate game state provider: " + context.getID());
		}

		registeredStateProviders.put(context.getID(), context);
	}

	public static void register(IProgramExtension extender) {
		if (registeredStateProviders.containsKey(extender.getID())) {
			throw new IllegalStateException("Duplicate shader extension: " + extender.getID());
		}

		registeredExtensions.put(extender.getID(), extender);
	}
}
