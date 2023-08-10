package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AllPlacementModifiers {
	private static final DeferredRegister<PlacementModifierType<?>> REGISTER = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, Create.ID);

	public static final RegistryObject<PlacementModifierType<ConfigDrivenPlacement>> CONFIG_DRIVEN = REGISTER.register("config_driven", () -> () -> ConfigDrivenPlacement.CODEC);

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
