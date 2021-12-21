package com.simibubi.create.lib.util;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.impl.tag.extension.TagDelegate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagUtil {
	private static final String CREATE = "create";
	private static final String COMMON = "c";
	private static final String TIC = "tconstruct";

	// blocks
	public static final Tag.Named<Block> WINDMILL_SAILS = new TagDelegate<>(new ResourceLocation(CREATE, "windmill_sails"), BlockTags::getAllTags);
	public static final Tag.Named<Block> FAN_HEATERS = new TagDelegate<>(new ResourceLocation(CREATE, "fan_heaters"), BlockTags::getAllTags);
	public static final Tag.Named<Block> WINDOWABLE = new TagDelegate<>(new ResourceLocation(CREATE, "windowable"), BlockTags::getAllTags);
	public static final Tag.Named<Block> NON_MOVABLE = new TagDelegate<>(new ResourceLocation(CREATE, "non_movable"), BlockTags::getAllTags);
	public static final Tag.Named<Block> BRITTLE = new TagDelegate<>(new ResourceLocation(CREATE, "brittle"), BlockTags::getAllTags);
	public static final Tag.Named<Block> BLOCKS$SEATS = new TagDelegate<>(new ResourceLocation(CREATE, "seats"), BlockTags::getAllTags);
	public static final Tag.Named<Block> SAILS = new TagDelegate<>(new ResourceLocation(CREATE, "sails"), BlockTags::getAllTags);
	public static final Tag.Named<Block> BLOCKS$VALVE_HANDLES = new TagDelegate<>(new ResourceLocation(CREATE, "valve_handles"), BlockTags::getAllTags);
	public static final Tag.Named<Block> FAN_TRANSPARENT = new TagDelegate<>(new ResourceLocation(CREATE, "fan_transparent"), BlockTags::getAllTags);
	public static final Tag.Named<Block> SAFE_NBT = new TagDelegate<>(new ResourceLocation(CREATE, "safe_nbt"), BlockTags::getAllTags);

	// items
	public static final Tag.Named<Item> CRUSHED_ORES = new TagDelegate<>(new ResourceLocation(CREATE, "crushed_ores"), ItemTags::getAllTags);
	public static final Tag.Named<Item> ITEMS$SEATS = new TagDelegate<>(new ResourceLocation(CREATE, "seats"), ItemTags::getAllTags);
	public static final Tag.Named<Item> ITEMS$VALVE_HANDLES = new TagDelegate<>(new ResourceLocation(CREATE, "valve_handles"), ItemTags::getAllTags);
	public static final Tag.Named<Item> UPRIGHT_ON_BELT = new TagDelegate<>(new ResourceLocation(CREATE, "upright_on_belt"), ItemTags::getAllTags);
	public static final Tag.Named<Item> CREATE_INGOTS = new TagDelegate<>(new ResourceLocation(CREATE, "create_ingots"), ItemTags::getAllTags);
	public static final Tag.Named<Item> BEACON_PAYMENT = new TagDelegate<>(new ResourceLocation(COMMON, "beacon_payment"), ItemTags::getAllTags);
	public static final Tag.Named<Item> INGOTS = new TagDelegate<>(new ResourceLocation(COMMON, "ingots"), ItemTags::getAllTags);
	public static final Tag.Named<Item> NUGGETS = new TagDelegate<>(new ResourceLocation(COMMON, "nuggets"), ItemTags::getAllTags);
	public static final Tag.Named<Item> PLATES = new TagDelegate<>(new ResourceLocation(COMMON, "plates"), ItemTags::getAllTags);
	public static final Tag.Named<Item> COBBLESTONE = new TagDelegate<>(new ResourceLocation(COMMON, "cobblestone"), ItemTags::getAllTags);

	// datagen tags

	public static final Tag.Named<Item> INGOTS_IRON = TagFactory.ITEM.create(new ResourceLocation("c:iron_ingots"));
	public static final Tag.Named<Item> DUSTS_GLOWSTONE = TagFactory.ITEM.create(new ResourceLocation("c:glowstone_dusts"));
	public static final Tag.Named<Item> COBBLESTONES = TagFactory.ITEM.create(new ResourceLocation("c:cobblestones"));
	public static final Tag.Named<Item> RODS_WOODEN = TagFactory.ITEM.create(new ResourceLocation("c:wood_sticks"));
	public static final Tag.Named<Item> DUSTS_REDSTONE = TagFactory.ITEM.create(new ResourceLocation("c:redstone_dusts"));
	public static final Tag.Named<Item> STONE = TagFactory.ITEM.create(new ResourceLocation("c:stone"));
	public static final Tag.Named<Item> STAINED_GLASS = TagFactory.ITEM.create(new ResourceLocation("c:stained_glass"));
	public static final Tag.Named<Item> STAINED_GLASS_PANES = TagFactory.ITEM.create(new ResourceLocation("c:stained_glass_panes"));
	public static final Tag.Named<Item> EGGS = TagFactory.ITEM.create(new ResourceLocation("c:eggs"));

	// Don't use these tags as they are here since more than one of common tags exists for them
//	public static final Tag.Named<Item> STICK_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:wooden_rods"));
//	public static final Tag.Named<Item> STONE_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:stones"));

	// TIC compat
	public static final Tag.Named<Block> SLIMY_LOGS = new TagDelegate<>(new ResourceLocation(TIC, "slimy_logs"), BlockTags::getAllTags);
	public static final Tag.Named<Item> SLIMEBALLS = new TagDelegate<>(new ResourceLocation(TIC, "slime_balls"), ItemTags::getAllTags);

	// dyes
	public static final Tag.Named<Item> BLACK_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "black_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> BLUE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "blue_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> BROWN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "brown_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> CYAN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "cyan_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> GRAY_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "gray_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> GREEN_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "green_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> LIGHT_BLUE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "light_blue_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> LIGHT_GRAY_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "light_gray_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> LIME_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "lime_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> MAGENTA_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "magenta_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> ORANGE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "orange_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> PINK_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "pink_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> PURPLE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "purple_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> RED_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "red_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> WHITE_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "white_dyes"), ItemTags::getAllTags);
	public static final Tag.Named<Item> YELLOW_DYES = new TagDelegate<>(new ResourceLocation(COMMON, "yellow_dyes"), ItemTags::getAllTags);

	// misc
	public static final Tag.Named<Fluid> MILK = new TagDelegate<>(new ResourceLocation(COMMON, "milk"), FluidTags::getAllTags);

	public static DyeColor getColorFromStack(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof DyeItem dyeItem) {
			return dyeItem.getDyeColor();
		}

		if (BLACK_DYES.contains(item)) return DyeColor.BLACK;
		if (BLUE_DYES.contains(item)) return DyeColor.BLUE;
		if (BROWN_DYES.contains(item)) return DyeColor.BROWN;
		if (CYAN_DYES.contains(item)) return DyeColor.CYAN;
		if (GRAY_DYES.contains(item)) return DyeColor.GRAY;
		if (GREEN_DYES.contains(item)) return DyeColor.GREEN;
		if (LIGHT_BLUE_DYES.contains(item)) return DyeColor.LIGHT_BLUE;
		if (LIGHT_GRAY_DYES.contains(item)) return DyeColor.LIGHT_GRAY;
		if (LIME_DYES.contains(item)) return DyeColor.LIME;
		if (MAGENTA_DYES.contains(item)) return DyeColor.MAGENTA;
		if (ORANGE_DYES.contains(item)) return DyeColor.ORANGE;
		if (PINK_DYES.contains(item)) return DyeColor.PINK;
		if (PURPLE_DYES.contains(item)) return DyeColor.PURPLE;
		if (RED_DYES.contains(item)) return DyeColor.RED;
		if (WHITE_DYES.contains(item)) return DyeColor.WHITE;
		if (YELLOW_DYES.contains(item)) return DyeColor.YELLOW;

		// item is not in color tags, default to white I guess
		return DyeColor.WHITE;
	}
}
