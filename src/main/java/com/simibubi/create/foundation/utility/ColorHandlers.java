package com.simibubi.create.foundation.utility;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.world.GrassColors;
import net.minecraft.world.biome.BiomeColors;

public class ColorHandlers {

	public static IBlockColor getGrassyBlock() {
		return (state, world, pos, layer) -> pos != null && world != null ? BiomeColors.getGrassColor(world, pos)
				: GrassColors.get(0.5D, 1.0D);
	}

	public static IItemColor getGrassyItem() {
		return (stack, layer) -> GrassColors.get(0.5D, 1.0D);
	}

	public static IBlockColor getRedstonePower() {
		return (state, world, pos, layer) -> RedstoneWireBlock
			.getWireColor(pos != null && world != null ? state.get(BlockStateProperties.POWER_0_15) : 0);
	}

}
