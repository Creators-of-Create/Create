package com.simibubi.create.content.contraptions.particle;

import com.mojang.serialization.Codec;

import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticle<T extends IParticleData> {

	IDeserializer<T> getDeserializer();

	Codec<T> getCodec(); 
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> getCodec() {
				return ICustomParticle.this.getCodec();
			}
		};
	}
	
	@OnlyIn(Dist.CLIENT)
	public IParticleMetaFactory<T> getFactory();
	
}
