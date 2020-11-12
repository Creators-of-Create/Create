package com.simibubi.create.content.contraptions.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticleData<T extends IParticleData> {

	public IDeserializer<T> getDeserializer();
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer());
	}
	
	@OnlyIn(Dist.CLIENT)
	public IParticleFactory<T> getFactory();
	
	@OnlyIn(Dist.CLIENT)
	public default void register(ParticleType<T> type, ParticleManager particles) {
		particles.registerFactory(type, getFactory());
	}
	
}
