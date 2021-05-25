package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.GrassColors;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AllColorHandlers {

	private final Map<Block, IBlockColor> coloredBlocks = new HashMap<>();
	private final Map<IItemProvider, IItemColor> coloredItems = new HashMap<>();

	//

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

	//

	public void register(Block block, IBlockColor color) {
		coloredBlocks.put(block, color);
	}

	public void register(IItemProvider item, IItemColor color) {
		coloredItems.put(item, color);
	}

	//

	@SubscribeEvent
	public void registerBlockColors(ColorHandlerEvent.Block event) {
		BlockColors blockColors = event.getBlockColors();
		coloredBlocks.forEach((block, color) -> blockColors.register(color, block));
	}

	@SubscribeEvent
	public void registerItemColors(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		coloredItems.forEach((item, color) -> itemColors.register(color, item));
	}

}
