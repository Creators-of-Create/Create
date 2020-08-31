package com.simibubi.create.foundation.utility;

import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

public class DyeHelper {

	public static IItemProvider getWoolOfDye(DyeColor color) {
		switch (color) {
		case BLACK:
			return Blocks.BLACK_WOOL;
		case BLUE:
			return Blocks.BLUE_WOOL;
		case BROWN:
			return Blocks.BROWN_WOOL;
		case CYAN:
			return Blocks.CYAN_WOOL;
		case GRAY:
			return Blocks.GRAY_WOOL;
		case GREEN:
			return Blocks.GREEN_WOOL;
		case LIGHT_BLUE:
			return Blocks.LIGHT_BLUE_WOOL;
		case LIGHT_GRAY:
			return Blocks.LIGHT_GRAY_WOOL;
		case LIME:
			return Blocks.LIME_WOOL;
		case MAGENTA:
			return Blocks.MAGENTA_WOOL;
		case ORANGE:
			return Blocks.ORANGE_WOOL;
		case PINK:
			return Blocks.PINK_WOOL;
		case PURPLE:
			return Blocks.PURPLE_WOOL;
		case RED:
			return Blocks.RED_WOOL;
		case YELLOW:
			return Blocks.YELLOW_WOOL;
		case WHITE:
		default:
			return Blocks.WHITE_WOOL;
		}
	}

	public static Tag<Item> getTagOfDye(DyeColor color) {
		switch (color) {
		case BLACK:
			return Tags.Items.DYES_BLACK;
		case BLUE:
			return Tags.Items.DYES_BLUE;
		case BROWN:
			return Tags.Items.DYES_BROWN;
		case CYAN:
			return Tags.Items.DYES_CYAN;
		case GRAY:
			return Tags.Items.DYES_GRAY;
		case GREEN:
			return Tags.Items.DYES_GREEN;
		case LIGHT_BLUE:
			return Tags.Items.DYES_LIGHT_BLUE;
		case LIGHT_GRAY:
			return Tags.Items.DYES_LIGHT_GRAY;
		case LIME:
			return Tags.Items.DYES_LIME;
		case MAGENTA:
			return Tags.Items.DYES_MAGENTA;
		case ORANGE:
			return Tags.Items.DYES_ORANGE;
		case PINK:
			return Tags.Items.DYES_PINK;
		case PURPLE:
			return Tags.Items.DYES_PURPLE;
		case RED:
			return Tags.Items.DYES_RED;
		case YELLOW:
			return Tags.Items.DYES_YELLOW;
		case WHITE:
		default:
			return Tags.Items.DYES_WHITE;
		}
	}
}
