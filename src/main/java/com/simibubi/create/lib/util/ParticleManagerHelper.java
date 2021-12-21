package com.simibubi.create.lib.util;

import com.simibubi.create.lib.extensions.ParticleEngineExtensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/**
 * Removal of parts of this class should be considered because Fabric API provides almost an exact replacement.
 * Use the Fabric API version instead of this class, unless issues arise.
 * @see net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
 */
@Deprecated
public final class ParticleManagerHelper {
	public static <T extends ParticleOptions> void registerFactory(ParticleEngine $this, ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> factory) {
		get($this).create$registerFactory0(type, factory);
	}

	private static ParticleEngineExtensions get(ParticleEngine manager) {
		return MixinHelper.cast(manager);
	}

	private ParticleManagerHelper() {}
}
