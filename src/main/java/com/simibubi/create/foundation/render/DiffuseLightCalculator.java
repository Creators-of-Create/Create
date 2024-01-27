package com.simibubi.create.foundation.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.model.lighting.QuadLighter;

public interface DiffuseLightCalculator {
	DiffuseLightCalculator DEFAULT = DiffuseLightCalculator::diffuseLight;
	DiffuseLightCalculator NETHER = DiffuseLightCalculator::diffuseLightNether;

	static DiffuseLightCalculator forLevel(ClientLevel level) {
		return level.effects().constantAmbientLight() ? NETHER : DEFAULT;
	}

	float getDiffuse(float normalX, float normalY, float normalZ, boolean shaded);

	static float diffuseLight(float x, float y, float z, boolean shaded) {
		if (!shaded) {
			return 1f;
		}
		return QuadLighter.calculateShade(x, y, z, false);
	}

	static float diffuseLightNether(float x, float y, float z, boolean shaded) {
		if (!shaded) {
			return 0.9f;
		}
		return QuadLighter.calculateShade(x, y, z, true);
	}
}
