package com.simibubi.create.content.equipment.bell;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;

@ParametersAreNonnullByDefault
public abstract class BasicParticleData<T> implements ParticleOptions, ICustomParticleDataWithSprite<BasicParticleData<T>> {

	public BasicParticleData() {
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BasicParticleData<T>> getStreamCodec() {
		return StreamCodec.unit(this);
	}

	@Override
	public MapCodec<BasicParticleData<T>> getCodec(ParticleType<BasicParticleData<T>> type) {
		return MapCodec.unit(this);
	}
}
