package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.contraptions.particle.CubeParticle;
import com.simibubi.create.content.contraptions.particle.CubeParticleData;
import com.simibubi.create.content.contraptions.particle.HeaterParticleData;
import com.simibubi.create.content.contraptions.particle.ICustomParticle;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllParticleTypes {

	ROTATION_INDICATOR(RotationIndicatorParticleData::new),
	AIR_FLOW(AirFlowParticleData::new),
	HEATER_PARTICLE(HeaterParticleData::new),
	CUBE(CubeParticleData::dummy, () -> CubeParticle.Factory::new)

	;

	private ParticleEntry<?> entry;

	<D extends IParticleData> AllParticleTypes(Supplier<? extends ICustomParticle<D>> typeFactory) {
		String asId = Lang.asId(this.name());
		entry = new ParticleEntry<>(new ResourceLocation(Create.ID, asId), typeFactory);
	}

	<D extends IParticleData> AllParticleTypes(Supplier<? extends ICustomParticle<D>> typeFactory,
		Supplier<Supplier<IParticleFactory<D>>> particleMetaFactory) {
		String asId = Lang.asId(this.name());
		entry = new ParticleEntry<>(new ResourceLocation(Create.ID, asId), typeFactory, particleMetaFactory);
	}

	public static void register(RegistryEvent.Register<ParticleType<?>> event) {
		for (AllParticleTypes particle : values())
			particle.entry.register(event.getRegistry());
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerFactories(ParticleFactoryRegisterEvent event) {
		ParticleManager particles = Minecraft.getInstance().particles;
		for (AllParticleTypes particle : values())
			particle.entry.registerFactory(particles);
	}

	public ParticleType<?> get() {
		return entry.getType();
	}

	public String parameter() {
		return Lang.asId(name());
	}

	private class ParticleEntry<D extends IParticleData> {
		Supplier<? extends ICustomParticle<D>> typeFactory;
		Supplier<Supplier<IParticleFactory<D>>> particleMetaFactory;
		ParticleType<D> type;
		ResourceLocation id;

		public ParticleEntry(ResourceLocation id, Supplier<? extends ICustomParticle<D>> typeFactory,
			Supplier<Supplier<IParticleFactory<D>>> particleMetaFactory) {
			this.id = id;
			this.typeFactory = typeFactory;
			this.particleMetaFactory = particleMetaFactory;
		}

		public ParticleEntry(ResourceLocation id, Supplier<? extends ICustomParticle<D>> typeFactory) {
			this(id, typeFactory, null);
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
				type = typeFactory.get()
					.createType();
				type.setRegistryName(id);
			}
		}

		@OnlyIn(Dist.CLIENT)
		void registerFactory(ParticleManager particles) {
			makeType();
			if (particleMetaFactory == null)
				particles.registerFactory(type, typeFactory.get()
					.getFactory());
			else
				particles.registerFactory(type, particleMetaFactory.get().get());
		}

	}

}
