package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.contraptions.particle.HeaterParticleData;
import com.simibubi.create.content.contraptions.particle.ICustomParticle;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
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
	HEATER_PARTICLE(HeaterParticleData::new)

	;

	private ParticleEntry<?> entry;

	private <D extends IParticleData> AllParticleTypes(Supplier<? extends ICustomParticle<D>> typeFactory) {
		String asId = Lang.asId(this.name());
		entry = new ParticleEntry<D>(new ResourceLocation(Create.ID, asId), typeFactory);
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
		ParticleType<D> type;
		ResourceLocation id;

		public ParticleEntry(ResourceLocation id, Supplier<? extends ICustomParticle<D>> typeFactory) {
			this.id = id;
			this.typeFactory = typeFactory;
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
			particles.registerFactory(type, typeFactory.get()
				.getFactory());
		}

	}

}
