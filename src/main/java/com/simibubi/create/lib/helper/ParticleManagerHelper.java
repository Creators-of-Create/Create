package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.extensions.ParticleEngineExtensions;
import com.simibubi.create.lib.mixin.accessor.ParticleEngineAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
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

	public static <T extends ParticleOptions> void registerFactory(ParticleEngine $this, ParticleType<T> type, ParticleProvider<T> factory) {
		get($this).create$registerFactory1(type, factory);
	}

	public static Int2ObjectMap<ParticleProvider<?>> getFactories(ParticleEngine manager) {
		return ((ParticleEngineAccessor) manager).getProviders();
	}

	private static ParticleEngineExtensions get(ParticleEngine manager) {
		return MixinHelper.cast(manager);
	}

	private ParticleManagerHelper() {}
}
