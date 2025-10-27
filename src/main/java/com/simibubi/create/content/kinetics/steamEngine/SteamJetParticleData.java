package com.simibubi.create.content.kinetics.steamEngine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SteamJetParticleData implements ParticleOptions, ICustomParticleDataWithSprite<SteamJetParticleData> {

	public static final MapCodec<SteamJetParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(Codec.FLOAT.fieldOf("speed")
			.forGetter(p -> p.speed))
		.apply(i, SteamJetParticleData::new));

	public static final StreamCodec<ByteBuf, SteamJetParticleData> STREAM_CODEC = ByteBufCodecs.FLOAT.map(
		SteamJetParticleData::new, p -> p.speed);

	float speed;

	public SteamJetParticleData(float speed) {
		this.speed = speed;
	}

	public SteamJetParticleData() {
		this(0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.STEAM_JET.get();
	}

	@Override
	public MapCodec<SteamJetParticleData> getCodec(ParticleType<SteamJetParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<SteamJetParticleData> getMetaFactory() {
		return SteamJetParticle.Factory::new;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, SteamJetParticleData> getStreamCodec() {
		return STREAM_CODEC;
	}
}
