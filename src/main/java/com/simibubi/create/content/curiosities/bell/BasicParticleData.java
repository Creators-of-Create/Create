package com.simibubi.create.content.curiosities.bell;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.simibubi.create.content.contraptions.particle.ICustomParticleDataWithSprite;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.particles.IParticleData.IDeserializer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicParticleData<T extends Particle> implements IParticleData, ICustomParticleDataWithSprite<BasicParticleData<T>> {

	public BasicParticleData() { }

	@Override
	public IDeserializer<BasicParticleData<T>> getDeserializer() {
		BasicParticleData<T> data = this;
		return new IParticleData.IDeserializer<BasicParticleData<T>>() {
			@Override
			public BasicParticleData<T> fromCommand(ParticleType<BasicParticleData<T>> arg0, StringReader reader) {
				return data;
			}

			@Override
			public BasicParticleData<T> fromNetwork(ParticleType<BasicParticleData<T>> type, PacketBuffer buffer) {
				return data;
			}
		};
	}

	@Override
	public Codec<BasicParticleData<T>> getCodec(ParticleType<BasicParticleData<T>> type) {
		return Codec.unit(this);
	}

	public interface IBasicParticleFactory<U extends Particle> {
		U makeParticle(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite sprite);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract IBasicParticleFactory<T> getBasicFactory();

	@Override
	@OnlyIn(Dist.CLIENT)
	public ParticleManager.IParticleMetaFactory<BasicParticleData<T>> getMetaFactory() {
		return animatedSprite -> (data, worldIn, x, y, z, vx, vy, vz) ->
				getBasicFactory().makeParticle(worldIn, x, y, z, vx, vy, vz, animatedSprite);
	}

	@Override
	public String writeToString() {
		return Registry.PARTICLE_TYPE.getKey(getType()).toString();
	}

	@Override
	public void writeToNetwork(PacketBuffer buffer) { }
}
