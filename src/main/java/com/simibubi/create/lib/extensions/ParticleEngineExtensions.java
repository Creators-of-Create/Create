package com.simibubi.create.lib.extensions;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

@Deprecated
public interface ParticleEngineExtensions {
	@ApiStatus.Internal
	<T extends ParticleOptions> void create$registerFactory0(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> spriteAwareFactory);

	@ApiStatus.Internal
	<T extends ParticleOptions> void create$registerFactory1(ParticleType<T> type, ParticleProvider<T> factory);
}
