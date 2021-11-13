package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;

@Environment(EnvType.CLIENT)
@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
	@Accessor("providers")
	Int2ObjectMap<ParticleProvider<?>> getProviders();
}
