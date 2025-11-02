package com.simibubi.create.infrastructure.data;

import java.util.Locale;
import java.util.function.BiConsumer;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllContraptionTypeTags;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.AllTags.AllMountedItemStorageTypeTags;
import com.simibubi.create.AllTags.AllRecipeSerializerTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.simibubi.create.foundation.data.recipe.CommonMetal.ItemLikeTag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Generate lang for tags.
 * Lang should only be generated for tags in namespaces you control.
 * This includes common tags, and a mod's own tags.
 */
public class TagLangGenerator {
	private final BiConsumer<String, String> output;

	public TagLangGenerator(BiConsumer<String, String> output) {
		this.output = output;
	}

	protected void translate(String key, String translation) {
		this.output.accept(key, translation);
	}

	protected void translate(TagKey<?> tag, String translation) {
		this.translate(keyFor(tag), translation);
	}

	private void translate(AllBlockTags tag, String translation) {
		this.translate(tag.tag, translation);
	}

	private void translate(AllItemTags tag, String translation) {
		this.translate(tag.tag, translation);
	}

	private void translate(AllFluidTags tag, String translation) {
		this.translate(tag.tag, translation);
	}

	private void translate(AllBlockTags block, AllItemTags item, String translation) {
		this.translate(block, translation);
		this.translate(item, translation);
	}

	private void translate(AllBlockTags block, AllFluidTags fluid, String translation) {
		this.translate(block, translation);
		this.translate(fluid, translation);
	}

	private void translate(ItemLikeTag tags, String translated) {
		this.translate(tags.blocks(), translated);
		this.translate(tags.items(), translated);
	}

	public void generate() {
		// blocks and block items
		translate(AllBlockTags.BRITTLE, "Brittle");
		translate(AllBlockTags.CASING, "Casings");
		translate(AllBlockTags.COPYCAT_ALLOW, "Copycat Copyable");
		translate(AllBlockTags.COPYCAT_DENY, "Not Copycat Copyable");
		translate(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING, AllFluidTags.FAN_PROCESSING_CATALYSTS_BLASTING, "Blasting Catalysts");
		translate(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING, AllFluidTags.FAN_PROCESSING_CATALYSTS_HAUNTING, "Haunting Catalysts");
		translate(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING, AllFluidTags.FAN_PROCESSING_CATALYSTS_SMOKING, "Smoking Catalysts");
		translate(AllBlockTags.FAN_PROCESSING_CATALYSTS_SPLASHING, AllFluidTags.FAN_PROCESSING_CATALYSTS_SPLASHING, "Splashing Catalysts");
		translate(AllBlockTags.FAN_TRANSPARENT, "Fan Transparent");
		translate(AllBlockTags.GIRDABLE_TRACKS, "Girdable Tracks");
		translate(AllBlockTags.MOVABLE_EMPTY_COLLIDER, "Movable Empty Colliders");
		translate(AllBlockTags.NON_MOVABLE, "Non-movable");
		translate(AllBlockTags.NON_BREAKABLE, "Non-breakable");
		translate(AllBlockTags.PASSIVE_BOILER_HEATERS, "Passive Boiler Heaters");
		translate(AllBlockTags.SAFE_NBT, "Safe NBT");
		translate(AllBlockTags.SEATS, AllItemTags.SEATS, "Seats");
		translate(AllBlockTags.POSTBOXES, AllItemTags.POSTBOXES, "Postboxes");
		translate(AllBlockTags.TABLE_CLOTHS, AllItemTags.TABLE_CLOTHS, "Table Cloths");
		translate(AllBlockTags.TOOLBOXES, AllItemTags.TOOLBOXES, "Toolboxes");
		translate(AllBlockTags.TRACKS, AllItemTags.TRACKS, "Tracks");
		translate(AllBlockTags.TREE_ATTACHMENTS, "Tree Attachments");
		translate(AllBlockTags.VALVE_HANDLES, AllItemTags.VALVE_HANDLES, "Valve Handles");
		translate(AllBlockTags.WINDMILL_SAILS, "Windmill Sails");
		translate(AllBlockTags.WRENCH_PICKUP, "Wrench-pickupable");
		translate(AllBlockTags.CHEST_MOUNTED_STORAGE, "Mounted Chests");
		translate(AllBlockTags.SIMPLE_MOUNTED_STORAGE, "Simple Mounted Storages");
		translate(AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST, "Non-mountable Storages");
		translate(AllBlockTags.ROOTS, "Roots");
		translate(AllBlockTags.SUGAR_CANE_VARIANTS, "Sugarcane-like");
		translate(AllBlockTags.NON_HARVESTABLE, "Non-harvestable");
		translate(AllBlockTags.SINGLE_BLOCK_INVENTORIES, "Single-block Inventories");
		translate(AllBlockTags.CARDBOARD_STORAGE_BLOCKS, AllItemTags.CARDBOARD_STORAGE_BLOCKS, "Cardboard Storage Blocks");
		translate(AllBlockTags.ANDESITE_ALLOY_STORAGE_BLOCKS, AllItemTags.ANDESITE_ALLOY_STORAGE_BLOCKS, "Andesite Alloy Storage Blocks");
		translate(AllBlockTags.CORALS, "Corals");

		// items
		translate(AllItemTags.BLAZE_BURNER_FUEL_REGULAR, "Regular Blaze Burner Fuel");
		translate(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL, "Special Blaze Burner Fuel");
		translate(AllItemTags.CASING, "Casings");
		translate(AllItemTags.CONTRAPTION_CONTROLLED, "Contraption-controllable");
		translate(AllItemTags.CREATE_INGOTS, "Create's Ingots");
		translate(AllItemTags.CRUSHED_RAW_MATERIALS, "Crushed Raw Materials");
		translate(AllItemTags.INVALID_FOR_TRACK_PAVING, "Track Paving Blacklist");
		translate(AllItemTags.DEPLOYABLE_DRINK, "Deployable Drink");
		translate(AllItemTags.PRESSURIZED_AIR_SOURCES, "Pressurized Air Sources");
		translate(AllItemTags.SANDPAPER, "Sandpaper");
		translate(AllItemTags.DYED_TABLE_CLOTHS, "Dyed Table Cloths");
		translate(AllItemTags.PULPIFIABLE, "Pulpifiable");
		translate(AllItemTags.SLEEPERS, "Sleepers");
		translate(AllItemTags.PACKAGES, "Packages");
		translate(AllItemTags.CHAIN_RIDEABLE, "Can Ride Chains");
		translate(AllItemTags.UPRIGHT_ON_BELT, "Upright on Belts");
		translate(AllItemTags.NOT_UPRIGHT_ON_BELT, "Not Upright on Belts");
		translate(AllItemTags.DISPENSE_BEHAVIOR_WRAP_BLACKLIST, "Dispense Behavior Wrap Blacklist");
		translate(AllItemTags.OBSIDIAN_DUST, "Obsidian Dust");
		translate(AllItemTags.PLATES, "Plates");
		translate(AllItemTags.OBSIDIAN_PLATES, "Obsidian Plates");
		translate(AllItemTags.CARDBOARD_PLATES, "Cardboard Plates");
		translate(AllItemTags.CERTUS_QUARTZ, "Certus Quartz");
		translate(AllItemTags.AMETRINE_ORES, "Ametrine Ores");
		translate(AllItemTags.ANTHRACITE_ORES, "Anthracite Ores");
		translate(AllItemTags.EMERALDITE_ORES, "Emeraldite Ores");
		translate(AllItemTags.LIGNITE_ORES, "Lignite Ores");
		translate(AllItemTags.HONEY_BUCKETS, "Honey Buckets");
		translate(AllItemTags.FLOURS, "Flours");
		translate(AllItemTags.WHEAT_FLOURS, "Wheat Flours");
		translate(AllItemTags.DOUGHS, "Doughs");
		translate(AllItemTags.WHEAT_DOUGHS, "Wheat Doughs");
		translate(AllItemTags.UA_CORAL, "Upgrade Aquatic Coral");

		// fluids
		translate(AllFluidTags.BOTTOMLESS_ALLOW, "Potentially Bottomless Fluids");
		translate(AllFluidTags.BOTTOMLESS_DENY, "Non-bottomless Fluids");
		translate(AllFluidTags.TEA, "Teas");
		translate(AllFluidTags.CHOCOLATE, "Chocolate");
		translate(AllFluidTags.CREOSOTE, "Creosote");

		// misc
		translate(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag, "Non-automatable");
		translate(AllContraptionTypeTags.OPENS_CONTROLS.tag, "Opens Contraption Controls");
		translate(AllContraptionTypeTags.REQUIRES_VEHICLE_FOR_RENDER.tag, "Requires a Vehicle to Render");
		translate(AllMountedItemStorageTypeTags.INTERNAL.tag, "Internal");
		translate(AllMountedItemStorageTypeTags.FUEL_BLACKLIST.tag, "Doesn't Provide Fuel");

		// palletes
		translate(AllItemTags.ALLURITE, "Allurite");
		translate(AllItemTags.AMETHYST, "Amethust");
		translate(AllItemTags.LUMIERE, "Lumiere");

		for (AllPaletteStoneTypes type : AllPaletteStoneTypes.values()) {
			translate(type.materialTag, toWord(type.name()));
		}

		// metals
		for (CommonMetal metal : CommonMetal.values()) {
			String name = toWord(metal.name);

			if (metal.isNatural) {
				translate(metal.ores, name + " Ores");
				translate(metal.rawOres, "Raw " + name + " Ores");
				translate(metal.rawStorageBlocks, "Raw " + name + " Storage Blocks");
			}

			translate(metal.ingots, name + " Ingots");
			translate(metal.storageBlocks, name + " Storage Blocks");
			translate(metal.nuggets, name + " Nuggets");
			translate(metal.plates, name + " Plates");
		}
	}

	protected static String keyFor(TagKey<?> tag) {
		ResourceLocation registryId = tag.registry().location();
		String registry = sanitize(
			registryId.getNamespace().equals("minecraft") ? registryId.getPath() : registryId.toLanguageKey()
		);

		return "tag." + registry + '.' + sanitize(tag.location().toLanguageKey());
	}

	private static String sanitize(String string) {
		return string.replace('/', '.');
	}

	/**
	 * Sets the first character to uppercase and all others to lowercase.
	 */
	protected static String toWord(String string) {
		if (string.isBlank())
			return string;

		String lower = string.toLowerCase(Locale.ROOT);
		char first = Character.toUpperCase(lower.charAt(0));
		String rest = lower.substring(1);
		return first + rest;
	}
}
