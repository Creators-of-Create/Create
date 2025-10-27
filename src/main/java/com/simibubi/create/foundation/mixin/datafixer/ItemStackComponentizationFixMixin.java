package com.simibubi.create.foundation.mixin.datafixer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Dynamic;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.zapper.PlacementPatterns;
import com.simibubi.create.content.equipment.zapper.terrainzapper.PlacementOptions;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainBrushes;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainTools;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;

import net.minecraft.util.StringRepresentable;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.world.item.DyeColor;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
	@Inject(method = "fixItemStack", at = @At("TAIL"))
	private static void create$fixItemsAndTranslateNBTIntoComponents(ItemStackComponentizationFix.ItemStackData stack, Dynamic<?> dynamic, CallbackInfo ci) {
		// create:symmetry_wand is skipped since it doesn't have anything important
		stack.removeTag("SequencedAssembly").result().ifPresent(d -> {
			float progress = d.get("Progress").asFloat(0);
			String id = d.get("id").asString().getOrThrow();
			int step = d.get("Step").asInt(1);

			Dynamic<?> seqDynamic = dynamic.emptyMap();
			seqDynamic = seqDynamic.set("progress", dynamic.createFloat(progress));
			seqDynamic = seqDynamic.set("step", dynamic.createInt(step));
			seqDynamic = seqDynamic.set("id", dynamic.createString(id));

			stack.setComponent("create:sequenced_assembly", seqDynamic);
		});

		if (stack.is("create:copper_backtank") || stack.is("create:netherite_backtank")) {
			stack.moveTagToComponent("Air", "create:banktank_air");
		}

		if (stack.is("create:belt_connector")) {
			stack.moveTagToComponent("FirstPulley", "create:belt_first_shaft");
		}

		if (stack.is("create:handheld_worldshaper")) {
			create$moveTagToEnumComponent(stack, "Pattern", "create:placement_pattern", PlacementPatterns.class);
			create$moveTagToEnumComponent(stack, "Brush", "create:shaper_brush", TerrainBrushes.class);
			stack.moveTagToComponent("BrushParams", "create:shaper_brush_params");
			create$moveTagToEnumComponent(stack, "Placement", "create:shaper_placement_options", PlacementOptions.class);
			create$moveTagToEnumComponent(stack, "Tool", "create:shaper_tool", TerrainTools.class);
			stack.moveTagToComponent("BlockUsed", "create:shaper_block_used");
			stack.moveTagToComponent("_Swap", "create:shaper_swap");
			stack.moveTagToComponent("BlockData", "create:shaper_block_data");
		}

		if (stack.is("create:filter")) {
			create$moveItemStackHandlerToItemContainerContents(stack, dynamic, "Items", "create:filter_items");
			stack.moveTagToComponent("RespectNBT", "create:filter_items_respect_nbt");
			stack.moveTagToComponent("Blacklist", "create:filter_items_blacklist");
		}

		// MatchedAttribute's won't be fixed, it's not really possible to write a fixer for that
		if (stack.is("create:attribute_filter")) {
			create$moveTagToEnumComponent(stack, "WhitelistMode", "create:filter_items", AttributeFilterWhitelistMode.class);
		}

		if (stack.is("create:clipboard")) {
			create$moveTagToEnumComponent(stack, "Type", "create:clipboard_type", ClipboardOverrides.ClipboardType.class);
			// Moves it from a boolean to a Unit (instance is the enum name)
			stack.removeTag("Readonly").result().ifPresent(itemDynamic -> {
				stack.setComponent("create:clipboard_read_only", itemDynamic.emptyMap().createString("instance"));
			});
			stack.moveTagToComponent("CopiedValues", "create:clipboard_copied_values");
			stack.moveTagToComponent("Pages", "create:clipboard_pages");
		}

		if (stack.is("create:track")) {
			stack.moveTagToComponent("ConnectingFrom", "create:track_connecting_from");
			stack.moveTagToComponent("ExtendCurve", "create:track_extended_curve");
		}

		if (stack.is(Set.of("create:track_station", "create:track_signal", "create:track_observer"))) {
			stack.moveTagToComponent("SelectedPos", "create:track_targeting_item_selected_pos");
			stack.moveTagToComponent("SelectedDirection", "create:track_targeting_item_selected_direction");
			stack.moveTagToComponent("Bezier", "track_targeting_item_bezier");
		}

		if (stack.is("create:schematic")) {
			stack.moveTagToComponent("Deployed", "create:schematic_deployed");
			stack.moveTagToComponent("Owner", "create:schematic_owner");
			stack.moveTagToComponent("File", "create:schematic_file");
			stack.moveTagToComponent("Anchor", "create:schematic_anchor");
			stack.moveTagToComponent("Rotation", "create:schematic_rotation");
			stack.moveTagToComponent("Mirror", "create:schematic_mirror");
			stack.moveTagToComponent("Bounds", "create:schematic_bounds");
		}

		if (stack.is("create:chromatic_compound")) {
			stack.moveTagToComponent("CollectingLight", "create:chromatic_compound_collecting_light");
		}

		if (stack.is(Set.of("create:sand_paper", "create:red_sand_paper"))) {
			stack.moveTagToComponent("Polishing", "create:sand_paper_polishing");
		}

		if (stack.is("create:minecart_contraption")) {
			stack.moveTagToComponent("Contraption", "create:minecart_contraption_data");
		}

		if (stack.is("create:linked_controller")) {
			create$moveItemStackHandlerToItemContainerContents(stack, dynamic, "Items", "create:linked_controller_items");
		}

		if (create$isCheckWithDyeColors(stack, "create:{}_toolbox")) {
			create$moveItemStackHandlerToItemContainerContents(stack, dynamic, "Inventory", "create:toolbox_inventory");
			stack.moveTagToComponent("UniqueId", "create:toolbox_uuid");
		}

		if (stack.is("create:schedule")) {
			stack.moveTagToComponent("Schedule", "create:train_schedule");
		}

		if (stack.is("create:display_link")) {
			stack.moveTagToComponent("SelectedPos", "create:display_link_selected_pos");
		}

		if (stack.is("create:schematicannon")) {
			stack.moveTagToComponent("BlockEntityTag.Options", "create:schematicannon_options");
		}
	}

	@Unique
	private static boolean create$isCheckWithDyeColors(ItemStackComponentizationFix.ItemStackData stack, String template) {
		Set<String> ids = new HashSet<>();
		for (DyeColor dyeColor : DyeColor.values()) {
			ids.add(template.replace("{}", dyeColor.getName()));
		}
		return stack.is(ids);
	}

	/**
	 * Converts an int nbt tag storing an enum via it's ordinal to a component storing the enum by using {@link StringRepresentable#fromValues}
	 *
	 * @param stack {@link ItemStackComponentizationFix.ItemStackData}
	 * @param key {@link String} The NBT Tag key that holds the int value
	 * @param component {@link String} The data component type ID
	 * @param enumClass The class holding the enum values
	 */
	@Unique
	private static <T extends Enum<?>> void create$moveTagToEnumComponent(ItemStackComponentizationFix.ItemStackData stack, String key, String component, Class<T> enumClass) {
		if (!enumClass.isEnum())
			throw new IllegalArgumentException("moveTagToEnumComponent must be called with a enum class!");

		T[] enumConstants = enumClass.getEnumConstants();

		stack.removeTag(key).result().ifPresent(itemDynamic -> {
			T enumConstant = enumConstants[itemDynamic.asInt(0)];
			String enumConstantName;

			if (enumConstant instanceof StringRepresentable stringRepresentable) {
				enumConstantName = stringRepresentable.getSerializedName();
			} else {
				throw new IllegalArgumentException("moveTagToEnumComponent must be called with a enum class that implements StringRepresentable!");
			}

			stack.setComponent(component, itemDynamic.emptyMap().createString(enumConstantName));
		});
	}

	@Unique
	private static void create$moveItemStackHandlerToItemContainerContents(ItemStackComponentizationFix.ItemStackData stack, Dynamic<?> dynamic, String key, String component) {
		try {
			stack.removeTag(key).result().ifPresent(itemDynamic -> {
				List<Dynamic<?>> list = new ArrayList<>();
				Stream<? extends Dynamic<?>> stream = itemDynamic.get("Items").asStream();
				stream.forEach(d -> {
					int slot = d.get("Slot").asInt(0);
					String id = d.get("id").asString().getOrThrow();
					int count = d.get("Count").asInt(1);

					Dynamic<?> dynamicMain = dynamic.emptyMap();
					Dynamic<?> dynamicItem = dynamic.emptyMap();

					dynamicItem = dynamicItem.set("count", dynamic.createInt(count));
					dynamicItem = dynamicItem.set("id", dynamic.createString(id));

					dynamicMain = dynamicMain.set("item", dynamicItem);
					dynamicMain = dynamicMain.set("slot", dynamic.createInt(slot));

					list.add(dynamicMain);
				});
				stack.setComponent(component, dynamic.createList(list.stream()));
			});
		} catch(Throwable ignored) {}
	}
}
