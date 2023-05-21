package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AllFeatures {
	private static final DeferredRegister<Feature<?>> REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, Create.ID);

	public static final RegistryObject<StandardOreFeature> STANDARD_ORE = REGISTER.register("standard_ore", () -> new StandardOreFeature());
	public static final RegistryObject<LayeredOreFeature> LAYERED_ORE = REGISTER.register("layered_ore", () -> new LayeredOreFeature());

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
