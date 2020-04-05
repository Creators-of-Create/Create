package com.simibubi.create;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateContainer;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateScreen;
import com.simibubi.create.modules.logistics.item.filter.AttributeFilterContainer;
import com.simibubi.create.modules.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.modules.logistics.item.filter.FilterContainer;
import com.simibubi.create.modules.logistics.item.filter.FilterScreen;
import com.simibubi.create.modules.schematics.block.SchematicTableContainer;
import com.simibubi.create.modules.schematics.block.SchematicTableScreen;
import com.simibubi.create.modules.schematics.block.SchematicannonContainer;
import com.simibubi.create.modules.schematics.block.SchematicannonScreen;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.ContainerType.IFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.network.IContainerFactory;

public enum AllContainers {

	SCHEMATIC_TABLE(SchematicTableContainer::new),
	SCHEMATICANNON(SchematicannonContainer::new),
	FLEXCRATE(FlexcrateContainer::new),
	FILTER(FilterContainer::new),
	ATTRIBUTE_FILTER(AttributeFilterContainer::new),

	;

	public ContainerType<? extends Container> type;
	private IFactory<?> factory;

	private <C extends Container> AllContainers(IContainerFactory<C> factory) {
		this.factory = factory;
	}

	public static void register(RegistryEvent.Register<ContainerType<?>> event) {
		for (AllContainers container : values()) {
			container.type = new ContainerType<>(container.factory)
					.setRegistryName(new ResourceLocation(Create.ID, Lang.asId(container.name())));
			event.getRegistry().register(container.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerScreenFactories() {
		bind(SCHEMATIC_TABLE, SchematicTableScreen::new);
		bind(SCHEMATICANNON, SchematicannonScreen::new);
		bind(FLEXCRATE, FlexcrateScreen::new);
		bind(FILTER, FilterScreen::new);
		bind(ATTRIBUTE_FILTER, AttributeFilterScreen::new);
	}

	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("unchecked")
	private static <C extends Container, S extends Screen & IHasContainer<C>> void bind(AllContainers c,
			IScreenFactory<C, S> factory) {
		ScreenManager.registerFactory((ContainerType<C>) c.type, factory);
	}

}
