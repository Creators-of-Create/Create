package com.simibubi.create.foundation.utility;

import static com.simibubi.create.AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT;
import static com.simibubi.create.AllBlocks.ANDESITE_ENCASED_SHAFT;
import static com.simibubi.create.AllBlocks.BRASS_BELT_FUNNEL;
import static com.simibubi.create.AllBlocks.BRASS_TUNNEL;
import static com.simibubi.create.AllBlocks.CONTENT_OBSERVER;
import static com.simibubi.create.AllBlocks.ENCASED_CHAIN_DRIVE;
import static com.simibubi.create.AllBlocks.LINEAR_CHASSIS;
import static com.simibubi.create.AllBlocks.MECHANICAL_DRILL;
import static com.simibubi.create.AllBlocks.MECHANICAL_HARVESTER;
import static com.simibubi.create.AllBlocks.MECHANICAL_PLOUGH;
import static com.simibubi.create.AllBlocks.MECHANICAL_SAW;
import static com.simibubi.create.AllBlocks.PISTON_EXTENSION_POLE;
import static com.simibubi.create.AllBlocks.POWERED_LATCH;
import static com.simibubi.create.AllBlocks.POWERED_TOGGLE_LATCH;
import static com.simibubi.create.AllBlocks.PULSE_EXTENDER;
import static com.simibubi.create.AllBlocks.PULSE_REPEATER;
import static com.simibubi.create.AllBlocks.RADIAL_CHASSIS;
import static com.simibubi.create.AllBlocks.REDSTONE_CONTACT;
import static com.simibubi.create.AllBlocks.REDSTONE_LINK;
import static com.simibubi.create.AllBlocks.SECONDARY_LINEAR_CHASSIS;
import static com.simibubi.create.AllBlocks.SPEEDOMETER;
import static com.simibubi.create.AllBlocks.STOCKPILE_SWITCH;
import static com.simibubi.create.AllBlocks.STRESSOMETER;
import static com.simibubi.create.AllItems.ATTRIBUTE_FILTER;
import static com.simibubi.create.AllItems.CRAFTER_SLOT_COVER;
import static com.simibubi.create.AllItems.GOLDEN_SHEET;
import static com.simibubi.create.AllItems.POWDERED_OBSIDIAN;
import static com.simibubi.create.AllItems.SCHEMATIC;
import static com.simibubi.create.AllItems.SCHEMATIC_AND_QUILL;
import static com.simibubi.create.AllItems.WAND_OF_SYMMETRY;
import static com.simibubi.create.AllItems.WHEAT_FLOUR;
import static com.simibubi.create.AllItems.WORLDSHAPER;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.ACACIA_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.ACACIA_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.BIRCH_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.BIRCH_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.DARK_OAK_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.DARK_OAK_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.JUNGLE_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.JUNGLE_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.OAK_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.OAK_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.ORNATE_IRON_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.ORNATE_IRON_WINDOW_PANE;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.SPRUCE_WINDOW;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.SPRUCE_WINDOW_PANE;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class RemapHelper {
	private static final Map<String, ResourceLocation> reMap = new HashMap<>();

	static {
		reMap.put("toggle_latch", POWERED_TOGGLE_LATCH.getId());
		reMap.put("encased_shaft", ANDESITE_ENCASED_SHAFT.getId());
		reMap.put("encased_belt", ENCASED_CHAIN_DRIVE.getId());
		reMap.put("adjustable_pulley", ADJUSTABLE_CHAIN_GEARSHIFT.getId());
		reMap.put("stockswitch", STOCKPILE_SWITCH.getId());
		reMap.put("redstone_latch", POWERED_LATCH.getId());
		reMap.put("contact", REDSTONE_CONTACT.getId());
		reMap.put("belt_funnel", BRASS_BELT_FUNNEL.getId());
		reMap.put("entity_detector", CONTENT_OBSERVER.getId());
		reMap.put("saw", MECHANICAL_SAW.getId());
		reMap.put("flexpulsepeater", PULSE_REPEATER.getId());
		reMap.put("stress_gauge", STRESSOMETER.getId());
		reMap.put("harvester", MECHANICAL_HARVESTER.getId());
		reMap.put("plough", MECHANICAL_PLOUGH.getId());
		reMap.put("drill", MECHANICAL_DRILL.getId());
		reMap.put("flexpeater", PULSE_EXTENDER.getId());
		reMap.put("rotation_chassis", RADIAL_CHASSIS.getId());
		reMap.put("belt_tunnel", BRASS_TUNNEL.getId());
		reMap.put("redstone_bridge", REDSTONE_LINK.getId());
		reMap.put("speed_gauge", SPEEDOMETER.getId());
		reMap.put("translation_chassis", LINEAR_CHASSIS.getId());
		reMap.put("translation_chassis_secondary", SECONDARY_LINEAR_CHASSIS.getId());
		reMap.put("piston_pole", PISTON_EXTENSION_POLE.getId());
		reMap.put("adjustable_pulse_repeater", PULSE_REPEATER.getId());
		reMap.put("adjustable_repeater", PULSE_REPEATER.getId());

		reMap.put("copper_block", Blocks.COPPER_BLOCK.getRegistryName());
		reMap.put("copper_ore", Blocks.COPPER_ORE.getRegistryName());

		reMap.put("acacia_glass", ACACIA_WINDOW.getId());
		reMap.put("acacia_glass_pane", ACACIA_WINDOW_PANE.getId());
		reMap.put("birch_glass", BIRCH_WINDOW.getId());
		reMap.put("birch_glass_pane", BIRCH_WINDOW_PANE.getId());
		reMap.put("dark_oak_glass", DARK_OAK_WINDOW.getId());
		reMap.put("dark_oak_glass_pane", DARK_OAK_WINDOW_PANE.getId());
		reMap.put("jungle_glass", JUNGLE_WINDOW.getId());
		reMap.put("jungle_glass_pane", JUNGLE_WINDOW_PANE.getId());
		reMap.put("oak_glass", OAK_WINDOW.getId());
		reMap.put("oak_glass_pane", OAK_WINDOW_PANE.getId());
		reMap.put("iron_glass", ORNATE_IRON_WINDOW.getId());
		reMap.put("iron_glass_pane", ORNATE_IRON_WINDOW_PANE.getId());
		reMap.put("spruce_glass", SPRUCE_WINDOW.getId());
		reMap.put("spruce_glass_pane", SPRUCE_WINDOW_PANE.getId());

		reMap.put("limestone_stairs", Create.asResource("polished_limestone_stairs"));
		reMap.put("weathered_limestone_layers", Create.asResource("layered_weathered_limestone"));
		reMap.put("indented_gabbro_slab", Create.asResource("polished_gabbro_slab"));
		reMap.put("andesite_layers", Create.asResource("layered_andesite"));
		reMap.put("scoria_layers", Create.asResource("layered_scoria"));
		reMap.put("dark_scoria_tiles_stairs", Create.asResource("dark_scoria_bricks_stairs"));
		reMap.put("dolomite_stairs", Create.asResource("polished_dolomite_stairs"));
		reMap.put("paved_gabbro_bricks", Create.asResource("paved_gabbro"));
		reMap.put("slightly_mossy_gabbro_bricks", Create.asResource("mossy_gabbro"));
		reMap.put("limestone_wall", Create.asResource("polished_limestone_wall"));
		reMap.put("dark_scoria_tiles", Create.asResource("dark_scoria_bricks"));
		reMap.put("dark_scoria_tiles_slab", Create.asResource("dark_scoria_bricks_slab"));
		reMap.put("weathered_limestone_stairs", Create.asResource("polished_weathered_limestone_stairs"));
		reMap.put("limestone_slab", Create.asResource("polished_limestone_slab"));
		reMap.put("scoria_slab", Create.asResource("polished_scoria_slab"));
		reMap.put("dolomite_wall", Create.asResource("polished_dolomite_wall"));
		reMap.put("gabbro_layers", Create.asResource("layered_gabbro"));
		reMap.put("scoria_wall", Create.asResource("polished_scoria_wall"));
		reMap.put("gabbro_slab", Create.asResource("polished_gabbro_slab"));
		reMap.put("dolomite_slab", Create.asResource("polished_dolomite_slab"));
		reMap.put("mossy_gabbro_bricks", Create.asResource("overgrown_gabbro"));
		reMap.put("paved_gabbro_bricks_slab", Create.asResource("paved_gabbro_slab"));
		reMap.put("gabbro_wall", Create.asResource("polished_gabbro_wall"));
		reMap.put("granite_layers", Create.asResource("layered_granite"));
		reMap.put("indented_gabbro", Create.asResource("polished_gabbro"));
		reMap.put("scoria_stairs", Create.asResource("polished_scoria_stairs"));
		reMap.put("weathered_limestone_wall", Create.asResource("polished_weathered_limestone_wall"));
		reMap.put("diorite_layers", Create.asResource("layered_diorite"));
		reMap.put("weathered_limestone_slab", Create.asResource("polished_weathered_limestone_slab"));
		reMap.put("gabbro_stairs", Create.asResource("polished_gabbro_stairs"));
		reMap.put("limestone_layers", Create.asResource("layered_limestone"));

		reMap.put("empty_blueprint", SCHEMATIC.getId());
		reMap.put("gold_sheet", GOLDEN_SHEET.getId());
		reMap.put("flour", WHEAT_FLOUR.getId());
		reMap.put("blueprint_and_quill", SCHEMATIC_AND_QUILL.getId());
		reMap.put("slot_cover", CRAFTER_SLOT_COVER.getId());
		reMap.put("blueprint", SCHEMATIC.getId());
		reMap.put("symmetry_wand", WAND_OF_SYMMETRY.getId());
		reMap.put("terrain_zapper", WORLDSHAPER.getId());
		reMap.put("property_filter", ATTRIBUTE_FILTER.getId());
		reMap.put("obsidian_dust", POWDERED_OBSIDIAN.getId());
	}

	@SubscribeEvent
	public static void remapBlocks(RegistryEvent.MissingMappings<Block> event) {
		for (Mapping<Block> mapping : event.getMappings(Create.ID)) {
			ResourceLocation key = mapping.key;
			String path = key.getPath();
			ResourceLocation remappedId = reMap.get(path);
			if (remappedId != null) {
				Block remapped = ForgeRegistries.BLOCKS.getValue(remappedId);
				if (remapped != null) {
					Create.LOGGER.warn("Remapping block '{}' to '{}'", key, remappedId);
					try {
						mapping.remap(remapped);
					} catch (Throwable t) {
						Create.LOGGER.warn("Remapping block '{}' to '{}' failed: {}", key, remappedId, t);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void remapItems(RegistryEvent.MissingMappings<Item> event) {
		for (Mapping<Item> mapping : event.getMappings(Create.ID)) {
			ResourceLocation key = mapping.key;
			String path = key.getPath();
			ResourceLocation remappedId = reMap.get(path);
			if (remappedId != null) {
				Item remapped = ForgeRegistries.ITEMS.getValue(remappedId);
				if (remapped != null) {
					Create.LOGGER.warn("Remapping item '{}' to '{}'", key, remappedId);
					try {
						mapping.remap(remapped);
					} catch (Throwable t) {
						Create.LOGGER.warn("Remapping item '{}' to '{}' failed: {}", key, remappedId, t);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void remapFluids(RegistryEvent.MissingMappings<Fluid> event) {
		for (Mapping<Fluid> mapping : event.getMappings(Create.ID)) {
			ResourceLocation key = mapping.key;
			String path = key.getPath();
			if (path.equals("milk"))
				mapping.remap(ForgeMod.MILK.get());
			else if (path.equals("flowing_milk"))
				mapping.remap(ForgeMod.FLOWING_MILK.get());
		}
	}

}
