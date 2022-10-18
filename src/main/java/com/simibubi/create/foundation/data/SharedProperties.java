package com.simibubi.create.foundation.data;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;

@MethodsReturnNonnullByDefault
public class SharedProperties {
	public static final Material BELT_MATERIAL =
		new Material(MaterialColor.COLOR_GRAY, false, true, true, true, false, false, PushReaction.NORMAL);

	public static final Material CRUSHING_WHEEL_CONTROLLER_MATERIAL =
		new Material(MaterialColor.NONE, false, false, true, true, false, false, PushReaction.BLOCK);

	public static Block wooden() {
		return Blocks.STRIPPED_SPRUCE_WOOD;
	}

	public static Block stone() {
		return Blocks.ANDESITE;
	}

	public static Block softMetal() {
		return Blocks.GOLD_BLOCK;
	}

	public static Block copperMetal() {
		return Blocks.COPPER_BLOCK;
	}

	public static Block netheriteMetal() {
		return Blocks.NETHERITE_BLOCK;
	}
}
