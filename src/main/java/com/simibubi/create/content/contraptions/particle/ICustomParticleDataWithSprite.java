package com.simibubi.create.content.contraptions.particle;

import com.mojang.serialization.Codec;
import com.simibubi.create.lib.util.ParticleManagerHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;

public interface ICustomParticleDataWithSprite<T extends ParticleOptions> extends ICustomParticleData<T> {

	Deserializer<T> getDeserializer();

	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> codec() {
				return ICustomParticleDataWithSprite.this.getCodec(this);
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	default ParticleProvider<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Environment(EnvType.CLIENT)
	public SpriteParticleRegistration<T> getMetaFactory();

	@Override
	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, ParticleEngine particles) {
		ParticleManagerHelper.registerFactory(particles, type, getMetaFactory());
	}

}
