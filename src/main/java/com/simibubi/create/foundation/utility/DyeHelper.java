package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.createmod.catnip.api.data.Couple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class DyeHelper {

	public static ItemLike getWoolOfDye(DyeColor color) {
		return WOOL_TABLE.getOrDefault(color, () -> Blocks.WOOL.white()).get();
	}

	public static Couple<Integer> getDyeColors(DyeColor color){
		return DYE_TABLE.getOrDefault(color, DYE_TABLE.get(DyeColor.WHITE));
	}

	/**
	 * Adds a dye color s.t. Create's blocks can use it instead of defaulting to white.
	 * @param color Dye color to add
	 * @param brightColor Front (bright) RGB color
	 * @param darkColor Back (dark) RGB color
	 * @param wool Supplier of wool item/block corresponding to the color
	 */
	public static void addDye(DyeColor color, Integer brightColor, Integer darkColor, Supplier<ItemLike> wool){
		DYE_TABLE.put(color, Couple.create(brightColor, darkColor));
		WOOL_TABLE.put(color, wool);
	}

	private static void addDye(DyeColor color, Integer brightColor, Integer darkColor, ItemLike wool){
		addDye(color, brightColor, darkColor, () -> wool);
	}

	private static final Map<DyeColor, Supplier<ItemLike>> WOOL_TABLE = new HashMap<>();

	private static final Map<DyeColor, Couple<Integer>> DYE_TABLE = new HashMap<>();

	static {
		// DyeColor, ( Front RGB, Back RGB )
		addDye(DyeColor.BLACK, 0x45403B, 0x21201F, Blocks.WOOL.black());
		addDye(DyeColor.RED, 0xB13937, 0x632737, Blocks.WOOL.red());
		addDye(DyeColor.GREEN, 0x208A46, 0x1D6045, Blocks.WOOL.green());
		addDye(DyeColor.BROWN, 0xAC855C, 0x68533E, Blocks.WOOL.brown());

		addDye(DyeColor.BLUE, 0x5391E1, 0x504B90, Blocks.WOOL.blue());
		addDye(DyeColor.GRAY, 0x5D666F, 0x313538, Blocks.WOOL.gray());
		addDye(DyeColor.LIGHT_GRAY, 0x95969B, 0x707070, Blocks.WOOL.lightGray());
		addDye(DyeColor.PURPLE, 0x9F54AE, 0x63366C, Blocks.WOOL.purple());

		addDye(DyeColor.CYAN, 0x3EABB4, 0x3C7872, Blocks.WOOL.cyan());
		addDye(DyeColor.PINK, 0xD5A8CB, 0xB86B95, Blocks.WOOL.pink());
		addDye(DyeColor.LIME, 0xA3DF55, 0x4FB16F, Blocks.WOOL.lime());
		addDye(DyeColor.YELLOW, 0xE6D756, 0xE9AC29, Blocks.WOOL.yellow());

		addDye(DyeColor.LIGHT_BLUE, 0x69CED2, 0x508AA5, Blocks.WOOL.lightBlue());
		addDye(DyeColor.ORANGE, 0xEE9246, 0xD94927, Blocks.WOOL.orange());
		addDye(DyeColor.MAGENTA, 0xF062B0, 0xC04488, Blocks.WOOL.magenta());
		addDye(DyeColor.WHITE, 0xEDEAE5, 0xBBB6B0, Blocks.WOOL.white());
	}
}
