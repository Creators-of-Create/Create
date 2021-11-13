package com.simibubi.create.lib.extensions;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;

public interface TerrainParticleExtensions {
	Particle create$updateSprite(BlockPos pos);
}
