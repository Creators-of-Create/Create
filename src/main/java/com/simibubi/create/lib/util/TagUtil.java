package com.simibubi.create.lib.util;

import static me.alphamode.forgetags.Tags.Items.DYES_BLACK;
import static me.alphamode.forgetags.Tags.Items.DYES_BLUE;
import static me.alphamode.forgetags.Tags.Items.DYES_BROWN;
import static me.alphamode.forgetags.Tags.Items.DYES_CYAN;
import static me.alphamode.forgetags.Tags.Items.DYES_GRAY;
import static me.alphamode.forgetags.Tags.Items.DYES_GREEN;
import static me.alphamode.forgetags.Tags.Items.DYES_LIGHT_BLUE;
import static me.alphamode.forgetags.Tags.Items.DYES_LIGHT_GRAY;
import static me.alphamode.forgetags.Tags.Items.DYES_LIME;
import static me.alphamode.forgetags.Tags.Items.DYES_MAGENTA;
import static me.alphamode.forgetags.Tags.Items.DYES_ORANGE;
import static me.alphamode.forgetags.Tags.Items.DYES_PINK;
import static me.alphamode.forgetags.Tags.Items.DYES_PURPLE;
import static me.alphamode.forgetags.Tags.Items.DYES_RED;
import static me.alphamode.forgetags.Tags.Items.DYES_WHITE;
import static me.alphamode.forgetags.Tags.Items.DYES_YELLOW;

import net.fabricmc.fabric.impl.tag.extension.TagDelegate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TagUtil {
	private static final String CREATE = "create";
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

	// Don't use these tags as they are here since more than one of common tags exists for them
//	public static final Tag.Named<Item> STICK_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:wooden_rods"));
//	public static final Tag.Named<Item> STONE_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:stones"));

	// TIC compat
	public static final Tag.Named<Block> SLIMY_LOGS = new TagDelegate<>(new ResourceLocation(TIC, "slimy_logs"), BlockTags::getAllTags);
	public static final Tag.Named<Item> SLIMEBALLS = new TagDelegate<>(new ResourceLocation(TIC, "slime_balls"), ItemTags::getAllTags);


	// misc

	public static DyeColor getColorFromStack(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof DyeItem dyeItem) {
			return dyeItem.getDyeColor();
		}

		if (DYES_BLACK.contains(item)) return DyeColor.BLACK;
		if (DYES_BLUE.contains(item)) return DyeColor.BLUE;
		if (DYES_BROWN.contains(item)) return DyeColor.BROWN;
		if (DYES_CYAN.contains(item)) return DyeColor.CYAN;
		if (DYES_GRAY.contains(item)) return DyeColor.GRAY;
		if (DYES_GREEN.contains(item)) return DyeColor.GREEN;
		if (DYES_LIGHT_BLUE.contains(item)) return DyeColor.LIGHT_BLUE;
		if (DYES_LIGHT_GRAY.contains(item)) return DyeColor.LIGHT_GRAY;
		if (DYES_LIME.contains(item)) return DyeColor.LIME;
		if (DYES_MAGENTA.contains(item)) return DyeColor.MAGENTA;
		if (DYES_ORANGE.contains(item)) return DyeColor.ORANGE;
		if (DYES_PINK.contains(item)) return DyeColor.PINK;
		if (DYES_PURPLE.contains(item)) return DyeColor.PURPLE;
		if (DYES_RED.contains(item)) return DyeColor.RED;
		if (DYES_WHITE.contains(item)) return DyeColor.WHITE;
		if (DYES_YELLOW.contains(item)) return DyeColor.YELLOW;

		// item is not in color tags, default to white I guess
		return DyeColor.WHITE;
	}
}
