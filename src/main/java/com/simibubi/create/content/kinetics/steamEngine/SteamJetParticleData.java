package com.simibubi.create.content.kinetics.steamEngine;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SteamJetParticleData implements ParticleOptions, ICustomParticleDataWithSprite<SteamJetParticleData> {

	public static final Codec<SteamJetParticleData> CODEC = RecordCodecBuilder.create(i -> i
		.group(Codec.FLOAT.fieldOf("speed")
			.forGetter(p -> p.speed))
		.apply(i, SteamJetParticleData::new));

	public static final ParticleOptions.Deserializer<SteamJetParticleData> DESERIALIZER =
		new ParticleOptions.Deserializer<SteamJetParticleData>() {
			public SteamJetParticleData fromCommand(ParticleType<SteamJetParticleData> particleTypeIn,
				StringReader reader) throws CommandSyntaxException {
				reader.expect(' ');
				float speed = reader.readFloat();
				return new SteamJetParticleData(speed);
			}

			public SteamJetParticleData fromNetwork(ParticleType<SteamJetParticleData> particleTypeIn,
				FriendlyByteBuf buffer) {
				return new SteamJetParticleData(buffer.readFloat());
			}
		};

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
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeFloat(speed);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %f", AllParticleTypes.STEAM_JET.parameter(), speed);
	}

	@Override
	public Deserializer<SteamJetParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	public Codec<SteamJetParticleData> getCodec(ParticleType<SteamJetParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<SteamJetParticleData> getMetaFactory() {
		return SteamJetParticle.Factory::new;
	}

}