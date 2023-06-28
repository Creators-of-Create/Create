package com.simibubi.create.foundation.particle;

import com.mojang.serialization.Codec;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

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
	@OnlyIn(Dist.CLIENT)
	default ParticleProvider<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}
	
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<T> getMetaFactory();
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public default void register(ParticleType<T> type, RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(type, getMetaFactory());
	}
	
}
