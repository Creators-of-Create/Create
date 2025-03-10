package com.simibubi.create.content.itemprocessing.specifics;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.item.ItemStack;

/**
 * indicates that particles are created while processing
 */
public interface IProduceParticles {

	void clearParticles();

	void addParticle(Particle particle);

	void addParticleItem(ItemStack itemStack);

}
