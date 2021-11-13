package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.content.contraptions.particle.CubeParticleData;
import com.simibubi.create.content.contraptions.particle.HeaterParticleData;
import com.simibubi.create.content.contraptions.particle.ICustomParticleData;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.content.curiosities.bell.SoulBaseParticle;
import com.simibubi.create.content.curiosities.bell.SoulParticle;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum AllParticleTypes {

	ROTATION_INDICATOR(RotationIndicatorParticleData::new),
	AIR_FLOW(AirFlowParticleData::new),
	AIR(AirParticleData::new),
	HEATER_PARTICLE(HeaterParticleData::new),
	CUBE(CubeParticleData::new),
	FLUID_PARTICLE(FluidParticleData::new),
	BASIN_FLUID(FluidParticleData::new),
	FLUID_DRIP(FluidParticleData::new),
	SOUL(SoulParticle.Data::new),
	SOUL_BASE(SoulBaseParticle.Data::new),
	SOUL_PERIMETER(SoulParticle.PerimeterData::new),
	SOUL_EXPANDING_PERIMETER(SoulParticle.ExpandingPerimeterData::new)
	;

	private ParticleEntry<?> entry;

	<D extends ParticleOptions> AllParticleTypes(Supplier<? extends ICustomParticleData<D>> typeFactory) {
		String asId = Lang.asId(this.name());
		entry = new ParticleEntry<>(new ResourceLocation(Create.ID, asId), typeFactory);
	}

	public static void register(RegistryEvent.Register<ParticleType<?>> event) {
		for (AllParticleTypes particle : values())
			particle.entry.register(event.getRegistry());
	}

	@Environment(EnvType.CLIENT)
	public static void registerFactories(ParticleFactoryRegisterEvent event) {
		ParticleEngine particles = Minecraft.getInstance().particleEngine;
		for (AllParticleTypes particle : values())
			particle.entry.registerFactory(particles);
	}

	public ParticleType<?> get() {
		return entry.getOrCreateType();
	}

	public String parameter() {
		return Lang.asId(name());
	}

	private class ParticleEntry<D extends ParticleOptions> {
		Supplier<? extends ICustomParticleData<D>> typeFactory;
		ParticleType<D> type;
		ResourceLocation id;

		public ParticleEntry(ResourceLocation id, Supplier<? extends ICustomParticleData<D>> typeFactory) {
			this.id = id;
			this.typeFactory = typeFactory;
		}

		void register(IForgeRegistry<ParticleType<?>> registry) {
			registry.register(getOrCreateType());
		}

		ParticleType<D> getOrCreateType() {
			if (type != null)
				return type;
			type = typeFactory.get()
				.createType();
			type.setRegistryName(id);
			return type;
		}

		@Environment(EnvType.CLIENT)
		void registerFactory(ParticleEngine particles) {
			typeFactory.get()
				.register(getOrCreateType(), particles);
		}

	}

}
