package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.foundation.block.IBlockVertexColor;
import com.simibubi.create.foundation.block.render.ColoredVertexModel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GrassColors;
import net.minecraft.world.ILightReader;
import net.minecraft.world.biome.BiomeColors;

public class AllColorHandlers {

	private final Map<Block, IBlockVertexColor> coloredVertexBlocks = new HashMap<>();
	private final Map<Block, IBlockColor> coloredBlocks = new HashMap<>();
	private final Map<IItemProvider, IItemColor> coloredItems = new HashMap<>();

	//

	public static IBlockColor getGrassyBlock() {
		return new BlockColor(
			(state, world, pos, layer) -> pos != null && world != null ? BiomeColors.getGrassColor(world, pos)
				: GrassColors.get(0.5D, 1.0D));
	}

	public static IItemColor getGrassyItem() {
		return new ItemColor((stack, layer) -> GrassColors.get(0.5D, 1.0D));
	}

	public static IBlockColor getRedstonePower() {
		return new BlockColor(
				(state, world, pos, layer) -> RedstoneWireBlock.colorMultiplier(pos != null && world != null ? state.get(BlockStateProperties.POWER_0_15) : 0)
		);
	}

	//

	public void register(Block block, IBlockColor color) {
		coloredBlocks.put(block, color);
	}

	public void register(Block block, IBlockVertexColor color) {
		coloredVertexBlocks.put(block, color);
	}

	public void register(IItemProvider item, IItemColor color) {
		coloredItems.put(item, color);
	}

	public void init() {
		BlockColors blockColors = Minecraft.getInstance()
			.getBlockColors();
		ItemColors itemColors = Minecraft.getInstance()
			.getItemColors();

		coloredBlocks.forEach((block, color) -> blockColors.register(color, block));
		coloredItems.forEach((item, color) -> itemColors.register(color, item));
		coloredVertexBlocks.forEach((block, color) -> CreateClient.getCustomBlockModels()
			.register(() -> block, model -> new ColoredVertexModel(model, color)));
	}

	//

	private static class ItemColor implements IItemColor {

		private Function function;

		@FunctionalInterface
		interface Function {
			int apply(ItemStack stack, int layer);
		}

		public ItemColor(Function function) {
			this.function = function;
		}

		@Override
		public int getColor(ItemStack stack, int layer) {
			return function.apply(stack, layer);
		}

	}

	private static class BlockColor implements IBlockColor {

		private Function function;

		@FunctionalInterface
		interface Function {
			int apply(BlockState state, ILightReader world, BlockPos pos, int layer);
		}

		public BlockColor(Function function) {
			this.function = function;
		}

		@Override
		public int getColor(BlockState state, ILightReader world, BlockPos pos, int layer) {
			return function.apply(state, world, pos, layer);
		}

	}

}
