package com.simibubi.create;

import com.simibubi.create.content.curiosities.toolbox.ToolboxMenu;
import com.simibubi.create.content.curiosities.toolbox.ToolboxScreen;
import com.simibubi.create.content.curiosities.tools.BlueprintMenu;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerMenu;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterMenu;
import com.simibubi.create.content.logistics.item.filter.FilterScreen;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleMenu;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.content.schematics.block.SchematicTableMenu;
import com.simibubi.create.content.schematics.block.SchematicTableScreen;
import com.simibubi.create.content.schematics.block.SchematicannonMenu;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.tterrag.registrate.builders.MenuBuilder.ForgeMenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AllMenuTypes {

	public static final MenuEntry<SchematicTableMenu> SCHEMATIC_TABLE =
		register("schematic_table", SchematicTableMenu::new, () -> SchematicTableScreen::new);

	public static final MenuEntry<SchematicannonMenu> SCHEMATICANNON =
		register("schematicannon", SchematicannonMenu::new, () -> SchematicannonScreen::new);

	public static final MenuEntry<FilterMenu> FILTER =
		register("filter", FilterMenu::new, () -> FilterScreen::new);

	public static final MenuEntry<AttributeFilterMenu> ATTRIBUTE_FILTER =
		register("attribute_filter", AttributeFilterMenu::new, () -> AttributeFilterScreen::new);

	public static final MenuEntry<BlueprintMenu> CRAFTING_BLUEPRINT =
		register("crafting_blueprint", BlueprintMenu::new, () -> BlueprintScreen::new);

	public static final MenuEntry<LinkedControllerMenu> LINKED_CONTROLLER =
		register("linked_controller", LinkedControllerMenu::new, () -> LinkedControllerScreen::new);
	
	public static final MenuEntry<ToolboxMenu> TOOLBOX =
		register("toolbox", ToolboxMenu::new, () -> ToolboxScreen::new);
	
	public static final MenuEntry<ScheduleMenu> SCHEDULE =
		register("schedule", ScheduleMenu::new, () -> ScheduleScreen::new);

	private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
		String name, ForgeMenuFactory<C> factory, NonNullSupplier<ScreenFactory<C, S>> screenFactory) {
		return Create.REGISTRATE
			.menu(name, factory, screenFactory)
			.register();
	}

	public static void register() {}

}
