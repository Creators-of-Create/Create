package com.simibubi.create.infrastructure.worldgen;

import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.worldgen.OreFeatureConfigEntry.DatagenExtension;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class BuiltinRegistration {
	private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, Create.ID);
	private static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, Create.ID);

	static {
		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
			ResourceLocation id = entry.getKey();
			if (id.getNamespace().equals(Create.ID)) {
				DatagenExtension datagenExt = entry.getValue().datagenExt();
				if (datagenExt != null) {
					CONFIGURED_FEATURE_REGISTER.register(id.getPath(), () -> datagenExt.createConfiguredFeature(BuiltinRegistries.ACCESS));
					PLACED_FEATURE_REGISTER.register(id.getPath(), () -> datagenExt.createPlacedFeature(BuiltinRegistries.ACCESS));
				}
			}
		}
	}

	public static void register(IEventBus modEventBus) {
		CONFIGURED_FEATURE_REGISTER.register(modEventBus);
		PLACED_FEATURE_REGISTER.register(modEventBus);
	}
}
