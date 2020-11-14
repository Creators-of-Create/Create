package com.simibubi.create.foundation.utility;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static com.simibubi.create.AllBlocks.*;
import static com.simibubi.create.AllItems.*;
import static com.simibubi.create.content.palettes.AllPaletteBlocks.*;

@Mod.EventBusSubscriber
@SuppressWarnings("unused")
public class RemapHelper {
	private static final Map<String, ResourceLocation> reMap = new HashMap<>();

	static {
		reMap.put("toggle_latch", POWERED_TOGGLE_LATCH.getId());
//		reMap.put("linked_extractor", );
		reMap.put("limestone_stairs", Create.asResource("polished_limestone_stairs"));
//		reMap.put("window_in_a_block", );
		reMap.put("weathered_limestone_layers", Create.asResource("layered_weathered_limestone"));
		reMap.put("stockswitch", STOCKPILE_SWITCH.getId());
		reMap.put("indented_gabbro_slab", Create.asResource("polished_gabbro_slab"));
//		reMap.put("vertical_extractor", );
		reMap.put("andesite_layers", Create.asResource("layered_andesite"));
		reMap.put("scoria_layers", Create.asResource("layered_scoria"));
//		reMap.put("extractor", );
//		reMap.put("linked_transposer", );
		reMap.put("dark_scoria_tiles_stairs", Create.asResource("dark_scoria_bricks_stairs"));
		reMap.put("redstone_latch", POWERED_LATCH.getId());
		reMap.put("oak_glass", OAK_WINDOW.getId());
		reMap.put("spruce_glass_pane", SPRUCE_WINDOW_PANE.getId());
		reMap.put("dolomite_stairs", Create.asResource("polished_dolomite_stairs"));
		reMap.put("contact", REDSTONE_CONTACT.getId());
		reMap.put("paved_gabbro_bricks", Create.asResource("paved_gabbro"));
		reMap.put("slightly_mossy_gabbro_bricks", Create.asResource("mossy_gabbro"));
		reMap.put("limestone_wall", Create.asResource("polished_limestone_wall"));
		reMap.put("acacia_glass_pane", ACACIA_WINDOW_PANE.getId());
		reMap.put("dark_oak_glass", DARK_OAK_WINDOW.getId());
//		reMap.put("vertical_linked_extractor", );
//		reMap.put("vertical_funnel", );
//		reMap.put("vertical_linked_transposer", );
		reMap.put("dark_oak_glass_pane", DARK_OAK_WINDOW_PANE.getId());
		reMap.put("belt_funnel", BRASS_BELT_FUNNEL.getId());
		reMap.put("dark_scoria_tiles", Create.asResource("dark_scoria_bricks"));
		reMap.put("acacia_glass", ACACIA_WINDOW.getId());
		reMap.put("dark_scoria_tiles_slab", Create.asResource("dark_scoria_bricks_slab"));
		reMap.put("weathered_limestone_stairs", Create.asResource("polished_weathered_limestone_stairs"));
		reMap.put("dolomite_layers", Create.asResource("layered_dolomite"));
		reMap.put("jungle_glass", JUNGLE_WINDOW.getId());
//		reMap.put("transposer", );
		reMap.put("iron_glass", ORNATE_IRON_WINDOW.getId());
		reMap.put("limestone_slab", Create.asResource("polished_limestone_slab"));
		reMap.put("entity_detector", CONTENT_OBSERVER.getId());
		reMap.put("flexcrate", ADJUSTABLE_CRATE.getId());
		reMap.put("scoria_slab", Create.asResource("polished_scoria_slab"));
		reMap.put("birch_glass", BIRCH_WINDOW.getId());
		reMap.put("saw", MECHANICAL_SAW.getId());
//		reMap.put("vertical_transposer", );
		reMap.put("flexpulsepeater", ADJUSTABLE_PULSE_REPEATER.getId());
		reMap.put("dolomite_wall", Create.asResource("polished_dolomite_wall"));
		reMap.put("gabbro_layers", Create.asResource("layered_gabbro"));
		reMap.put("scoria_wall", Create.asResource("polished_scoria_wall"));
		reMap.put("stress_gauge", STRESSOMETER.getId());
		reMap.put("gabbro_slab", Create.asResource("polished_gabbro_slab"));
		reMap.put("spruce_glass", SPRUCE_WINDOW.getId());
//		reMap.put("cocoa_log", );
		reMap.put("iron_glass_pane", ORNATE_IRON_WINDOW_PANE.getId());
		reMap.put("birch_glass_pane", BIRCH_WINDOW_PANE.getId());
		reMap.put("harvester", MECHANICAL_HARVESTER.getId());
		reMap.put("dolomite_slab", Create.asResource("polished_dolomite_slab"));
		reMap.put("plough", MECHANICAL_PLOUGH.getId());
		reMap.put("mossy_gabbro_bricks", Create.asResource("overgrown_gabbro"));
		reMap.put("paved_gabbro_bricks_slab", Create.asResource("paved_gabbro_slab"));
		reMap.put("gabbro_wall", Create.asResource("polished_gabbro_wall"));
		reMap.put("granite_layers", Create.asResource("layered_granite"));
		reMap.put("indented_gabbro", Create.asResource("polished_gabbro"));
		reMap.put("drill", MECHANICAL_DRILL.getId());
		reMap.put("flexpeater", ADJUSTABLE_REPEATER.getId());
		reMap.put("rotation_chassis", RADIAL_CHASSIS.getId());
		reMap.put("scoria_stairs", Create.asResource("polished_scoria_stairs"));
		reMap.put("weathered_limestone_wall", Create.asResource("polished_weathered_limestone_wall"));
		reMap.put("belt_tunnel", BRASS_TUNNEL.getId());
		reMap.put("redstone_bridge", REDSTONE_LINK.getId());
		reMap.put("speed_gauge", SPEEDOMETER.getId());
		reMap.put("diorite_layers", Create.asResource("layered_diorite"));
		reMap.put("oak_glass_pane", OAK_WINDOW_PANE.getId());
		reMap.put("translation_chassis", LINEAR_CHASSIS.getId());
//		reMap.put("symmetry_tripleplane", Create.asResource(""));
		reMap.put("weathered_limestone_slab", Create.asResource("polished_weathered_limestone_slab"));
		reMap.put("gabbro_stairs", Create.asResource("polished_gabbro_stairs"));
		reMap.put("limestone_layers", Create.asResource("layered_limestone"));
//		reMap.put("symmetry_plane", Create.asResource(""));
		reMap.put("translation_chassis_secondary", SECONDARY_LINEAR_CHASSIS.getId());
		reMap.put("jungle_glass_pane", JUNGLE_WINDOW_PANE.getId());
		reMap.put("piston_pole", PISTON_EXTENSION_POLE.getId());

//		reMap.put("shadow_steel_sword", );
		reMap.put("lapis_plate", LAPIS_SHEET.getId());
		reMap.put("crushed_copper", CRUSHED_COPPER.getId());
		reMap.put("empty_blueprint", SCHEMATIC.getId());
//		reMap.put("shadow_steel_mattock", );
//		reMap.put("rose_quartz_sword", );
		reMap.put("gold_sheet", GOLDEN_SHEET.getId());
		reMap.put("flour", WHEAT_FLOUR.getId());
//		reMap.put("encased_shaft", );
		reMap.put("blueprint_and_quill", SCHEMATIC_AND_QUILL.getId());
		reMap.put("crushed_iron", CRUSHED_IRON.getId());
//		reMap.put("blazing_axe", );
		reMap.put("slot_cover", CRAFTER_SLOT_COVER.getId());
		reMap.put("blueprint", SCHEMATIC.getId());
		reMap.put("symmetry_wand", WAND_OF_SYMMETRY.getId());
		reMap.put("terrain_zapper", WORLDSHAPER.getId());
//		reMap.put("blazing_sword", );
		reMap.put("zinc_handle", HAND_CRANK.getId());
//		reMap.put("rose_quartz_axe", );
//		reMap.put("shadow_steel_pickaxe", );
		reMap.put("placement_handgun", BLOCKZAPPER.getId());
		reMap.put("crushed_zinc", CRUSHED_ZINC.getId());
//		reMap.put("rose_quartz_pickaxe", );
//		reMap.put("blazing_pickaxe", );
		reMap.put("property_filter", ATTRIBUTE_FILTER.getId());
//		reMap.put("blazing_shovel", );
		reMap.put("crushed_gold", CRUSHED_GOLD.getId());
		reMap.put("obsidian_dust", POWDERED_OBSIDIAN.getId());
//		reMap.put("rose_quartz_shovel", );
	}

	@SubscribeEvent
	public static void onRemapBlocks(RegistryEvent.MissingMappings<Block> event) {
		ModContainer mod = ModList.get().getModContainerById(Create.ID).orElse(null);
		if (mod == null)
			return;
		event.setModContainer(mod);
		ImmutableList<RegistryEvent.MissingMappings.Mapping<Block>> mappings = event.getMappings();

		for (RegistryEvent.MissingMappings.Mapping<Block> mapping : mappings) {
			if (reMap.containsKey(mapping.key.getPath())) {
				try {
					Create.logger.warn("Remapping block '{}' to '{}'", mapping.key, reMap.get(mapping.key.getPath()));
					mapping.remap(ForgeRegistries.BLOCKS.getValue(reMap.get(mapping.key.getPath())));
				} catch (Throwable t) {
					Create.logger.warn("Remapping block '{}' to '{}' failed: {}", mapping.key, reMap.get(mapping.key.getPath()), t);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRemapItems(RegistryEvent.MissingMappings<Item> event) {
		ModContainer mod = ModList.get().getModContainerById(Create.ID).orElse(null);
		if (mod == null)
			return;
		event.setModContainer(mod);
		ImmutableList<RegistryEvent.MissingMappings.Mapping<Item>> mappings = event.getMappings();

		for (RegistryEvent.MissingMappings.Mapping<Item> mapping : mappings) {
			if (reMap.containsKey(mapping.key.getPath())) {
				try {
					Create.logger.warn("Remapping item '{}' to '{}'", mapping.key, reMap.get(mapping.key.getPath()));
					mapping.remap(ForgeRegistries.ITEMS.getValue(reMap.get(mapping.key.getPath())));
				} catch (Throwable t) {
					Create.logger.warn("Remapping item '{}' to '{}' failed: {}", mapping.key, reMap.get(mapping.key.getPath()), t);
				}
			}
		}
	}
}
