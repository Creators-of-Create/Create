package com.simibubi.create.foundation.data;

import mcp.MethodsReturnNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;

@MethodsReturnNonnullByDefault
public class SharedProperties {
	public static Material beltMaterial =
		new Material(MaterialColor.GRAY, false, true, true, true, true, false, false, PushReaction.NORMAL);

	public static Block stone() {
		return Blocks.ANDESITE;
	}

	public static Block softMetal() {
		return Blocks.GOLD_BLOCK;
	}

	public static Block wooden() {
		return Blocks.STRIPPED_SPRUCE_WOOD;
	}
}
