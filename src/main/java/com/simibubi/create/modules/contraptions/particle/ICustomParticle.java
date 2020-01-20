package com.simibubi.create.modules.contraptions.particle;

import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticle<T extends IParticleData> {

	public IDeserializer<T> getDeserializer();
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer());
	}
	
	@OnlyIn(Dist.CLIENT)
	public IParticleMetaFactory<T> getFactory();
	
}
