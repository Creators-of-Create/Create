package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.ParticleAccessor;

import net.minecraft.client.particle.Particle;

public final class ParticleHelper {
	public static boolean getStoppedByCollision(Particle particle) {
		return ((ParticleAccessor) particle).create$stoppedByCollision();
	}

	public static void setStoppedByCollision(Particle particle, boolean bool) {
		((ParticleAccessor) particle).create$stoppedByCollision(bool);
	}

	private ParticleHelper() {}
}
