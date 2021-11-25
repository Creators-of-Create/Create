package com.simibubi.create;

import javax.annotation.Nonnull;

import com.simibubi.create.api.contraption.ContraptionStorageRegistry;
import com.simibubi.create.content.contraptions.components.storage.CreativeCrateRegistry;
import com.simibubi.create.content.contraptions.components.storage.FlexCrateRegistry;
import com.simibubi.create.content.contraptions.components.storage.VanillaStorageRegistry;

import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Create.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllContraptionStorages {

	public static Lazy<ContraptionStorageRegistry> ADJUSTABLE_CRATE = ContraptionStorageRegistry.getInstance("create:crate");
	public static Lazy<ContraptionStorageRegistry> CREATIVE_CRATE = ContraptionStorageRegistry.getInstance("create:creative_crate");
	public static Lazy<ContraptionStorageRegistry> VANILLA = ContraptionStorageRegistry.getInstance("create:vanilla_storage");

	public static void register(IEventBus modEventBus) {
		ContraptionStorageRegistry.STORAGES.register(modEventBus);
	}

	@SubscribeEvent
	public static void registerModules(@Nonnull RegistryEvent.Register<ContraptionStorageRegistry> event) {
		IForgeRegistry<ContraptionStorageRegistry> registry = event.getRegistry();
		ContraptionStorageRegistry.register(registry, "create:crate", FlexCrateRegistry::new);
		ContraptionStorageRegistry.register(registry, "create:creative_crate", CreativeCrateRegistry::new);
		ContraptionStorageRegistry.register(registry, "create:vanilla_storage", VanillaStorageRegistry::new);
	}

	@SubscribeEvent
	public static void init(final FMLCommonSetupEvent event) {
		ContraptionStorageRegistry.initCache();
	}
}
