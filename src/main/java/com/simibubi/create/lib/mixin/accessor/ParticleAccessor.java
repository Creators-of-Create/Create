package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;

@Environment(EnvType.CLIENT)
@Mixin(Particle.class)
public interface ParticleAccessor {
	@Accessor("stoppedByCollision")
	void create$stoppedByCollision(boolean stoppedByCollision);
}
