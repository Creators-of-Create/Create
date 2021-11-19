package com.simibubi.create.content.contraptions.particle;

import com.mojang.serialization.Codec;

import com.simibubi.create.lib.helper.ParticleHelper;

import com.simibubi.create.lib.helper.ParticleManagerHelper;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ICustomParticleData<T extends ParticleOptions> {

	Deserializer<T> getDeserializer();

	Codec<T> getCodec(ParticleType<T> type);

	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> codec() {
				return ICustomParticleData.this.getCodec(this);
			}
		};
	}

	@Environment(EnvType.CLIENT)
	public ParticleProvider<T> getFactory();

	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, ParticleEngine particles) {
		ParticleFactoryRegistry.getInstance().register(type, getFactory());
	}

}
