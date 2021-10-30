package com.simibubi.create.foundation.utility;

import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.util.IItemProvider;

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

}
