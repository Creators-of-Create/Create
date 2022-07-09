package com.simibubi.create;

import com.simibubi.create.content.curiosities.toolbox.ToolboxContainer;
import com.simibubi.create.content.curiosities.toolbox.ToolboxScreen;
import com.simibubi.create.content.curiosities.tools.BlueprintContainer;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerContainer;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterContainer;
import com.simibubi.create.content.logistics.item.filter.FilterScreen;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleContainer;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.content.schematics.block.SchematicTableContainer;
import com.simibubi.create.content.schematics.block.SchematicTableScreen;
import com.simibubi.create.content.schematics.block.SchematicannonContainer;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.tterrag.registrate.builders.MenuBuilder.ForgeMenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AllContainerTypes {

	public static final MenuEntry<SchematicTableContainer> SCHEMATIC_TABLE =
		register("schematic_table", SchematicTableContainer::new, () -> SchematicTableScreen::new);

	public static final MenuEntry<SchematicannonContainer> SCHEMATICANNON =
		register("schematicannon", SchematicannonContainer::new, () -> SchematicannonScreen::new);

	public static final MenuEntry<FilterContainer> FILTER =
		register("filter", FilterContainer::new, () -> FilterScreen::new);

	public static final MenuEntry<AttributeFilterContainer> ATTRIBUTE_FILTER =
		register("attribute_filter", AttributeFilterContainer::new, () -> AttributeFilterScreen::new);

	public static final MenuEntry<BlueprintContainer> CRAFTING_BLUEPRINT =
		register("crafting_blueprint", BlueprintContainer::new, () -> BlueprintScreen::new);

	public static final MenuEntry<LinkedControllerContainer> LINKED_CONTROLLER =
		register("linked_controller", LinkedControllerContainer::new, () -> LinkedControllerScreen::new);
	
	public static final MenuEntry<ToolboxContainer> TOOLBOX =
		register("toolbox", ToolboxContainer::new, () -> ToolboxScreen::new);
	
	public static final MenuEntry<ScheduleContainer> SCHEDULE =
		register("schedule", ScheduleContainer::new, () -> ScheduleScreen::new);

	private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
		String name, ForgeMenuFactory<C> factory, NonNullSupplier<ScreenFactory<C, S>> screenFactory) {
		return Create.registrate()
			.menu(name, factory, screenFactory)
			.register();
	}

	public static void register() {}

}
