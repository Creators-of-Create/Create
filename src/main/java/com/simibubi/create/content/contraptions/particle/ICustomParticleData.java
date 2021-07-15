package com.simibubi.create.content.contraptions.particle;

import com.mojang.serialization.Codec;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticleData<T extends IParticleData> {

	IDeserializer<T> getDeserializer();

	Codec<T> getCodec(ParticleType<T> type); 
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> codec() {
				return ICustomParticleData.this.getCodec(this);
			}
		};
	}
	
	@OnlyIn(Dist.CLIENT)
	public IParticleFactory<T> getFactory();
	
	@OnlyIn(Dist.CLIENT)
	public default void register(ParticleType<T> type, ParticleManager particles) {
		particles.register(type, getFactory());
	}
	
}
