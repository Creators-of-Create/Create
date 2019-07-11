package com.simibubi.create;

import com.simibubi.create.block.SchematicTableContainer;
import com.simibubi.create.gui.SchematicTableScreen;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public enum AllContainers {

	SchematicTable();

	public ContainerType<? extends Container> type;

	private AllContainers() {
	}

	@SubscribeEvent
	public static void onContainerTypeRegistry(final RegistryEvent.Register<ContainerType<?>> e) {
		SchematicTable.type = new ContainerType<>(SchematicTableContainer::new)
				.setRegistryName(SchematicTable.name().toLowerCase());
		
		e.getRegistry().register(SchematicTable.type);
	}

	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	public static void registerScreenFactories() {
		ScreenManager.<SchematicTableContainer, SchematicTableScreen>registerFactory(
				(ContainerType<SchematicTableContainer>) SchematicTable.type, SchematicTableScreen::new);
	}

}
