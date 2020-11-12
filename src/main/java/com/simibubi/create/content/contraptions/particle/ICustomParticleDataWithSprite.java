package com.simibubi.create.content.contraptions.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticleDataWithSprite<T extends IParticleData> extends ICustomParticleData<T> {

	public IDeserializer<T> getDeserializer();
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer());
	}
	
	@Override
	default IParticleFactory<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}
	
	@OnlyIn(Dist.CLIENT)
	public IParticleMetaFactory<T> getMetaFactory();
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public default void register(ParticleType<T> type, ParticleManager particles) {
		particles.registerFactory(type, getMetaFactory());
	}
	
}
