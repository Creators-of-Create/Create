package com.simibubi.create.foundation.utility;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

public class DyeHelper {

	public static ItemLike getWoolOfDye(DyeColor color) {
		return switch (color) {
			case BLACK -> Blocks.BLACK_WOOL;
			case BLUE -> Blocks.BLUE_WOOL;
			case BROWN -> Blocks.BROWN_WOOL;
			case CYAN -> Blocks.CYAN_WOOL;
			case GRAY -> Blocks.GRAY_WOOL;
			case GREEN -> Blocks.GREEN_WOOL;
			case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
			case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
			case LIME -> Blocks.LIME_WOOL;
			case MAGENTA -> Blocks.MAGENTA_WOOL;
			case ORANGE -> Blocks.ORANGE_WOOL;
			case PINK -> Blocks.PINK_WOOL;
			case PURPLE -> Blocks.PURPLE_WOOL;
			case RED -> Blocks.RED_WOOL;
			case YELLOW -> Blocks.YELLOW_WOOL;
			default -> Blocks.WHITE_WOOL;
		};
	}

	@Nullable
	public static DyeColor getColor(ItemStack item) {
		if (item.getItem() instanceof DyeItem dyeable) {
			return dyeable.getDyeColor();
		}
		return null;
	}
}
