package com.simibubi.create.foundation.utility;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class DyeHelper {

	public static ItemLike getWoolOfDye(DyeColor color) {
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

	public static final Map<DyeColor, Couple<Integer>> DYE_TABLE = new ImmutableMap.Builder<DyeColor, Couple<Integer>>()
	
	// DyeColor, ( Front RGB, Back RGB )
	.put(DyeColor.BLACK, Couple.create(0x45403B, 0x21201F))
	.put(DyeColor.RED, Couple.create(0xB13937, 0x632737))
	.put(DyeColor.GREEN, Couple.create(0x208A46, 0x1D6045))
	.put(DyeColor.BROWN, Couple.create(0xAC855C, 0x68533E))
	
	.put(DyeColor.BLUE, Couple.create(0x5391E1, 0x504B90))
	.put(DyeColor.GRAY, Couple.create(0x5D666F, 0x313538))
	.put(DyeColor.LIGHT_GRAY, Couple.create(0x95969B, 0x707070))
	.put(DyeColor.PURPLE, Couple.create(0x9F54AE, 0x63366C))
	
	.put(DyeColor.CYAN, Couple.create(0x3EABB4, 0x3C7872))
	.put(DyeColor.PINK, Couple.create(0xD5A8CB, 0xB86B95))
	.put(DyeColor.LIME, Couple.create(0xA3DF55, 0x4FB16F))
	.put(DyeColor.YELLOW, Couple.create(0xE6D756, 0xE9AC29))
	
	.put(DyeColor.LIGHT_BLUE, Couple.create(0x69CED2, 0x508AA5))
	.put(DyeColor.ORANGE, Couple.create(0xEE9246, 0xD94927))
	.put(DyeColor.MAGENTA, Couple.create(0xF062B0, 0xC04488))
	.put(DyeColor.WHITE, Couple.create(0xEDEAE5, 0xBBB6B0))
	
	.build();

}
