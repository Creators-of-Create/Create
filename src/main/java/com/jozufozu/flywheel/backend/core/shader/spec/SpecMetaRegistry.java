package com.jozufozu.flywheel.backend.core.shader.spec;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.core.shader.WorldFog;
import com.jozufozu.flywheel.backend.core.shader.extension.IProgramExtension;
import com.jozufozu.flywheel.backend.core.shader.gamestate.FogStateProvider;
import com.jozufozu.flywheel.backend.core.shader.gamestate.IGameStateProvider;
import com.jozufozu.flywheel.backend.core.shader.gamestate.NormalDebugStateProvider;
import com.jozufozu.flywheel.backend.core.shader.gamestate.RainbowDebugStateProvider;

import net.minecraft.util.ResourceLocation;

public class SpecMetaRegistry {

	private static final Map<ResourceLocation, IProgramExtension> registeredExtensions = new HashMap<>();
	private static final Map<ResourceLocation, IGameStateProvider> registeredStateProviders = new HashMap<>();

	// TODO: proper registration, don't call this from ShaderLoader
	private static boolean initialized = false;
	public static void init() {
		if (initialized) return;
		initialized = true;

		register(FogStateProvider.INSTANCE);
		register(RainbowDebugStateProvider.INSTANCE);
		register(NormalDebugStateProvider.INSTANCE);

		register(WorldFog.LINEAR);
		register(WorldFog.EXP2);
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
