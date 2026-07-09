package com.simibubi.create;

import com.simibubi.create.content.equipment.bell.SoulBaseParticle;
import com.simibubi.create.content.equipment.bell.SoulParticle;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.fluids.particle.FluidStackParticle;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticle;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.fan.AirFlowParticle;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticle;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.content.trains.CubeParticle;
import com.simibubi.create.content.trains.CubeParticleData;
import com.simibubi.create.foundation.particle.AirParticle;
import com.simibubi.create.foundation.particle.AirParticleData;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class AllParticleTypesClient {

	public static void registerFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(type(AllParticleTypes.ROTATION_INDICATOR), RotationIndicatorParticle.Factory::new);
		event.registerSpriteSet(type(AllParticleTypes.AIR_FLOW), AirFlowParticle.Factory::new);
		event.registerSpriteSet(type(AllParticleTypes.AIR), AirParticle.Factory::new);
		event.registerSpriteSet(type(AllParticleTypes.STEAM_JET), SteamJetParticle.Factory::new);
		event.registerSpecial(type(AllParticleTypes.CUBE), new CubeParticle.Factory());
		registerFluidParticle(event, AllParticleTypes.FLUID_PARTICLE);
		registerFluidParticle(event, AllParticleTypes.BASIN_FLUID);
		registerFluidParticle(event, AllParticleTypes.FLUID_DRIP);
		event.registerSpriteSet(type(AllParticleTypes.WIFI), sprite -> (data, world, x, y, z, vx, vy, vz, random) ->
			new WiFiParticle(world, x, y, z, vx, vy, vz, sprite));
		event.registerSpriteSet(type(AllParticleTypes.SOUL), sprite -> (data, world, x, y, z, vx, vy, vz, random) ->
			new SoulParticle(world, x, y, z, vx, vy, vz, sprite, data));
		event.registerSpriteSet(type(AllParticleTypes.SOUL_BASE), sprite -> (data, world, x, y, z, vx, vy, vz, random) ->
			new SoulBaseParticle(world, x, y, z, vx, vy, vz, sprite));
		event.registerSpriteSet(type(AllParticleTypes.SOUL_PERIMETER), sprite -> (data, world, x, y, z, vx, vy, vz, random) ->
			new SoulParticle(world, x, y, z, vx, vy, vz, sprite, data));
		event.registerSpriteSet(type(AllParticleTypes.SOUL_EXPANDING_PERIMETER), sprite -> (data, world, x, y, z, vx, vy, vz, random) ->
			new SoulParticle(world, x, y, z, vx, vy, vz, sprite, data));
	}

	private static void registerFluidParticle(RegisterParticleProvidersEvent event, AllParticleTypes type) {
		ParticleType<FluidParticleData> particleType = type(type);
		event.registerSpecial(particleType, (data, world, x, y, z, vx, vy, vz, random) ->
			FluidStackParticle.create(particleType, world, data.getFluid(), x, y, z, vx, vy, vz));
	}

	@SuppressWarnings("unchecked")
	private static <T extends ParticleOptions> ParticleType<T> type(AllParticleTypes type) {
		return (ParticleType<T>) type.get();
	}

}
