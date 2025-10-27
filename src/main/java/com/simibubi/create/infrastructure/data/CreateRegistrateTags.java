package com.simibubi.create.infrastructure.data;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllEntityTags;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.TagGen.CreateTagsProvider;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.Tags;

public class CreateRegistrateTags {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	private static final Block[] SHULKER_BOXES = {
		Blocks.SHULKER_BOX,
		Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX,
		Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
		Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
		Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX
	};

	public static void addGenerators() {
		REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CreateRegistrateTags::genBlockTags);
		REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CreateRegistrateTags::genItemTags);
		REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, CreateRegistrateTags::genFluidTags);
		REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, CreateRegistrateTags::genEntityTags);
	}

	private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
		CreateTagsProvider<Block> prov = new CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);

		prov.tag(AllBlockTags.BRITTLE.tag)
			.add(Blocks.BELL, Blocks.COCOA, Blocks.FLOWER_POT, Blocks.MOSS_CARPET, Blocks.BAMBOO_SAPLING,
				Blocks.BIG_DRIPLEAF, Blocks.VINE, Blocks.TWISTING_VINES_PLANT, Blocks.TWISTING_VINES,
				Blocks.WEEPING_VINES_PLANT, Blocks.WEEPING_VINES, Blocks.CAKE
			)
			.addTag(AllBlockTags.CORALS.tag)
			.addTag(BlockTags.CAVE_VINES)
			.addTag(BlockTags.BANNERS)
			.addTag(BlockTags.BEDS)
			.addTag(BlockTags.DOORS);

		prov.tag(AllBlockTags.COPYCAT_ALLOW.tag)
			.add(Blocks.BARREL);

		prov.tag(AllBlockTags.COPYCAT_DENY.tag)
			.addTag(BlockTags.CAULDRONS)
			.addTag(BlockTags.SAPLINGS)
			.addTag(BlockTags.CLIMBABLE);

		prov.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING.tag)
			.add(Blocks.SOUL_FIRE)
			.add(Blocks.SOUL_CAMPFIRE);

		prov.tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag)
			.add(Blocks.FIRE)
			.add(Blocks.CAMPFIRE);

		prov.tag(AllBlockTags.FAN_TRANSPARENT.tag)
			.add(Blocks.IRON_BARS)
			.add(Blocks.MANGROVE_ROOTS)
			.add(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE,
				Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE,
				Blocks.WAXED_WEATHERED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE
			)
			.addTag(BlockTags.CAMPFIRES)
			.addTag(BlockTags.FENCES)
			.addTag(BlockTags.LEAVES);

		prov.tag(AllBlockTags.MOVABLE_EMPTY_COLLIDER.tag)
			.add(Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.BAMBOO_SAPLING,
				Blocks.VINE, Blocks.TWISTING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.WEEPING_VINES_PLANT,
				Blocks.WEEPING_VINES
			)
			.addTag(AllBlockTags.CORALS.tag)
			.addTag(BlockTags.CAVE_VINES)
			.addTag(BlockTags.FENCE_GATES)
			.addTag(BlockTags.BANNERS);

		prov.tag(AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
			.add(Blocks.MAGMA_BLOCK, Blocks.LAVA)
			.addTag(BlockTags.CAMPFIRES)
			.addTag(BlockTags.FIRE);

		prov.tag(AllBlockTags.SAFE_NBT.tag)
			.addTag(BlockTags.BANNERS)
			.addTag(BlockTags.ALL_SIGNS);

		prov.tag(AllBlockTags.TREE_ATTACHMENTS.tag)
			.add(Blocks.BEE_NEST, Blocks.COCOA, Blocks.MANGROVE_PROPAGULE, Blocks.MOSS_CARPET, Blocks.SHROOMLIGHT, Blocks.VINE);

		prov.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.addTag(BlockTags.WOOL);

		prov.tag(AllBlockTags.WRENCH_PICKUP.tag)
			.add(Blocks.REDSTONE_WIRE, Blocks.REDSTONE_TORCH, Blocks.REPEATER, Blocks.LEVER, Blocks.REDSTONE_LAMP,
				Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.REDSTONE_WALL_TORCH, Blocks.PISTON, Blocks.STICKY_PISTON,
				Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.DAYLIGHT_DETECTOR, Blocks.TARGET, Blocks.HOPPER)
			.addTag(BlockTags.BUTTONS)
			.addTag(BlockTags.PRESSURE_PLATES)
			.addTag(BlockTags.RAILS);

		// tags aren't used here because the implementations of modded entries are unknown
		prov.tag(AllBlockTags.CHEST_MOUNTED_STORAGE.tag).add(
			Blocks.CHEST, Blocks.TRAPPED_CHEST
		);
		prov.tag(AllBlockTags.SIMPLE_MOUNTED_STORAGE.tag)
			.add(Blocks.BARREL)
			.add(SHULKER_BOXES);

		prov.tag(AllBlockTags.SINGLE_BLOCK_INVENTORIES.tag)
			.add(SHULKER_BOXES)
			.add(Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER, Blocks.CHISELED_BOOKSHELF, Blocks.JUKEBOX)
			.addTag(Tags.Blocks.BARRELS);

		prov.tag(AllBlockTags.ROOTS.tag)
			.add(Blocks.MANGROVE_ROOTS);

		prov.tag(AllBlockTags.SUGAR_CANE_VARIANTS.tag)
			.add(Blocks.SUGAR_CANE);

		prov.tag(AllBlockTags.NON_HARVESTABLE.tag)
			.add(Blocks.FIRE);

		prov.tag(AllBlockTags.CORALS.tag)
			.add(Blocks.DEAD_TUBE_CORAL, Blocks.DEAD_BRAIN_CORAL, Blocks.DEAD_BUBBLE_CORAL, Blocks.DEAD_FIRE_CORAL,
				Blocks.DEAD_HORN_CORAL, Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL,
				Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.DEAD_TUBE_CORAL_FAN,
				Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN,
				Blocks.DEAD_HORN_CORAL_FAN, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN,
				Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN,
				Blocks.DEAD_TUBE_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_WALL_FAN,
				Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_WALL_FAN,
				Blocks.DEAD_HORN_CORAL_WALL_FAN, Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN,
				Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN
			);

		// COMPAT

		TagGen.addOptional(prov.tag(AllBlockTags.NON_MOVABLE.tag), Mods.IE, List.of(
			"connector_lv", "connector_lv_relay", "connector_mv", "connector_mv_relay",
			"connector_hv", "connector_hv_relay", "connector_bundled", "connector_structural",
			"connector_redstone", "connector_probe", "breaker_switch"));

		TagGen.addOptional(prov.tag(AllBlockTags.ROOTS.tag), Mods.TF,
			List.of("root", "liveroot_block", "mangrove_root"));
	}

	private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
		CreateTagsProvider<Item> prov = new CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

		prov.tag(AllItemTags.CHAIN_RIDEABLE.tag)
			.addTag(Tags.Items.TOOLS_WRENCH);

		prov.tag(AllItemTags.PULPIFIABLE.tag)
			.add(Items.BAMBOO, Items.SUGAR_CANE)
			.addTag(ItemTags.SAPLINGS);

		prov.tag(AllItemTags.SLEEPERS.tag)
			.add(Items.STONE_SLAB, Items.SMOOTH_STONE_SLAB, Items.ANDESITE_SLAB);

		prov.tag(AllItemTags.DEPLOYABLE_DRINK.tag)
			.add(Items.MILK_BUCKET, Items.POTION);

		prov.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.add(Items.GLASS_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
				Items.HONEY_BOTTLE, Items.CAKE, Items.BOWL, Items.MUSHROOM_STEW, Items.RABBIT_STEW,
				Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);

		prov.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.add(Items.BELL, Items.CAMPFIRE, Items.SOUL_CAMPFIRE, Items.DISPENSER, Items.DROPPER);

		prov.tag(ItemTags.BEACON_PAYMENT_ITEMS)
			.addTag(AllItemTags.CREATE_INGOTS.tag);

		prov.tag(Tags.Items.INGOTS)
			.addTag(AllItemTags.CREATE_INGOTS.tag);

		prov.tag(AllItemTags.OBSIDIAN_DUST.tag).add(AllItems.POWDERED_OBSIDIAN.get());

		prov.tag(Tags.Items.ENCHANTABLES).addTag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag);

		prov.tag(ItemTags.TRIMMABLE_ARMOR)
			.remove(
				AllItems.COPPER_DIVING_BOOTS.getId(),
				AllItems.COPPER_BACKTANK.getId(),
				AllItems.COPPER_DIVING_HELMET.getId(),
				AllItems.NETHERITE_DIVING_BOOTS.getId(),
				AllItems.NETHERITE_BACKTANK.getId(),
				AllItems.NETHERITE_DIVING_HELMET.getId()
			);

		prov.tag(ItemTags.DURABILITY_ENCHANTABLE)
			.addTag(AllItemTags.SANDPAPER.tag);

		// COMPAT

		prov.tag(AllItemTags.CURIOS_HEAD.tag)
			.add(AllItems.GOGGLES.get());

		TagGen.addOptional(prov.tag(AllItemTags.ALLURITE.tag), Mods.GS, gsPalette("allurite"));

		TagGen.addOptional(prov.tag(AllItemTags.LUMIERE.tag), Mods.GS, gsPalette("lumiere"));

		TagGen.addOptional(prov.tag(AllItemTags.AMETHYST.tag), Mods.GS, gsPalette("amethyst"));

		TagGen.addOptional(prov.tag(AllItemTags.UA_CORAL.tag), Mods.UA, List.of("acan_coral",
			"finger_coral", "star_coral", "moss_coral", "petal_coral", "branch_coral",
			"rock_coral", "pillow_coral", "chrome_coral", "silk_coral"));

		TagGen.addOptional(prov.tag(AllItemTags.UPRIGHT_ON_BELT.tag), Mods.ATM, List.of(
			"orange_pudding", "orange_sorbet", "passion_fruit_sorbet", "aloe_gel_bottle"));
	}

	private static ArrayList<String> gsPalette(String material) {
		ArrayList<String> toReturn = new ArrayList<>();
		toReturn.add(material + "_block");
		toReturn.add(material + "_stairs");
		toReturn.add(material + "_slab");
		toReturn.add("smooth_" + material);
		toReturn.add("smooth_" + material + "_stairs");
		toReturn.add("smooth_" + material + "_slab");
		toReturn.add(material + "_bricks");
		toReturn.add(material + "_brick_stairs");
		toReturn.add(material + "_brick_slab");
		toReturn.add("chiseled_" + material);
		return toReturn;
	}

	private static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
		CreateTagsProvider<Fluid> prov = new CreateTagsProvider<>(provIn, Fluid::builtInRegistryHolder);

		prov.tag(AllFluidTags.BOTTOMLESS_ALLOW.tag)
			.add(Fluids.WATER, Fluids.LAVA);

		prov.tag(AllFluidTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag)
			.add(Fluids.LAVA, Fluids.FLOWING_LAVA);

		prov.tag(AllFluidTags.FAN_PROCESSING_CATALYSTS_SPLASHING.tag)
			.add(Fluids.WATER, Fluids.FLOWING_WATER);
	}

	private static void genEntityTags(RegistrateTagsProvider<EntityType<?>> provIn) {
		CreateTagsProvider<EntityType<?>> prov = new CreateTagsProvider<>(provIn, EntityType::builtInRegistryHolder);

		prov.tag(AllEntityTags.BLAZE_BURNER_CAPTURABLE.tag)
			.add(EntityType.BLAZE);

		prov.tag(AllEntityTags.IGNORE_SEAT.tag)
			.addTag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED);
	}
}
