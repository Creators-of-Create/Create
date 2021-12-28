package com.simibubi.create.lib.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;

public interface BlockParticleOptionExtensions {
	BlockParticleOption create$setPos(BlockPos pos);
	BlockPos create$getPos();
}
