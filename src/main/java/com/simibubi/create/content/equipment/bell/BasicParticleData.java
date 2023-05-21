package com.simibubi.create.content.equipment.bell;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicParticleData<T extends Particle> implements ParticleOptions, ICustomParticleDataWithSprite<BasicParticleData<T>> {

	public BasicParticleData() { }

	@Override
	public Deserializer<BasicParticleData<T>> getDeserializer() {
		BasicParticleData<T> data = this;
		return new ParticleOptions.Deserializer<BasicParticleData<T>>() {
			@Override
			public BasicParticleData<T> fromCommand(ParticleType<BasicParticleData<T>> arg0, StringReader reader) {
				return data;
			}

			@Override
			public BasicParticleData<T> fromNetwork(ParticleType<BasicParticleData<T>> type, FriendlyByteBuf buffer) {
				return data;
			}
		};
	}

	@Override
	public Codec<BasicParticleData<T>> getCodec(ParticleType<BasicParticleData<T>> type) {
		return Codec.unit(this);
	}

	public interface IBasicParticleFactory<U extends Particle> {
		U makeParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprite);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract IBasicParticleFactory<T> getBasicFactory();

	@Override
	@OnlyIn(Dist.CLIENT)
	public ParticleEngine.SpriteParticleRegistration<BasicParticleData<T>> getMetaFactory() {
		return animatedSprite -> (data, worldIn, x, y, z, vx, vy, vz) ->
				getBasicFactory().makeParticle(worldIn, x, y, z, vx, vy, vz, animatedSprite);
	}

	@Override
	public String writeToString() {
		return RegisteredObjects.getKeyOrThrow(getType()).toString();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer) { }
}
