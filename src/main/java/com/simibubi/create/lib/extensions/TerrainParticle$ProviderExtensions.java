package com.simibubi.create.lib.extensions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;

public interface TerrainParticle$ProviderExtensions {
	Particle create$makeParticleAtPos(BlockParticleOption blockParticleData, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i);

	Particle create$updateSprite(BlockPos pos);
}
