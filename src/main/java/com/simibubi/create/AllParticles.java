package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.particle.RotationIndicatorParticle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllParticles {

	ROTATION_INDICATOR(RotationIndicatorParticle.Type::new, RotationIndicatorParticle.Factory::new),

	;

	private ParticleEntry<?> entry;

	private <D extends IParticleData> AllParticles(Supplier<? extends ParticleType<D>> typeFactory,
			IParticleMetaFactory<D> particleFactory) {
		String asId = Lang.asId(this.name().toLowerCase());
		entry = new ParticleEntry<D>(new ResourceLocation(Create.ID, asId), typeFactory, particleFactory);
	}

	public static void register(RegistryEvent.Register<ParticleType<?>> event) {
		for (AllParticles particle : values())
			particle.entry.register(event.getRegistry());
	}

	public static void registerFactories(ParticleFactoryRegisterEvent event) {
		ParticleManager particles = Minecraft.getInstance().particles;
		for (AllParticles particle : values())
			particle.entry.registerFactory(particles);
	}

	public ParticleType<?> get() {
		return entry.getType();
	}

	public String parameter() {
		return Lang.asId(name());
	}

	private class ParticleEntry<D extends IParticleData> {
		Supplier<? extends ParticleType<D>> typeFactory;
		IParticleMetaFactory<D> particleFactory;
		ParticleType<D> type;
		ResourceLocation id;

		public ParticleEntry(ResourceLocation id, Supplier<? extends ParticleType<D>> typeFactory,
				IParticleMetaFactory<D> particleFactory) {
			this.id = id;
			this.typeFactory = typeFactory;
			this.particleFactory = particleFactory;
		}

		ParticleType<?> getType() {
			makeType();
			return type;
		}

		void register(IForgeRegistry<ParticleType<?>> registry) {
			makeType();
			registry.register(type);
		}

		void makeType() {
			if (type == null) {
				type = typeFactory.get();
				type.setRegistryName(id);
			}
		}

		void registerFactory(ParticleManager particles) {
			makeType();
			particles.registerFactory(type, particleFactory);
		}

	}

}
