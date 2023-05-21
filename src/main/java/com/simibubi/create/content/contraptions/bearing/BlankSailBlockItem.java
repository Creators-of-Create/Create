package com.simibubi.create.content.contraptions.bearing;

import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BlankSailBlockItem extends BlockItem {
	public BlankSailBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
		super.registerBlocks(blockToItemMap, item);
		for (BlockEntry<SailBlock> entry : AllBlocks.DYED_SAILS) {
			blockToItemMap.put(entry.get(), item);
		}
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item item) {
		super.removeFromBlockToItemMap(blockToItemMap, item);
		for (BlockEntry<SailBlock> entry : AllBlocks.DYED_SAILS) {
			blockToItemMap.remove(entry.get());
		}
	}
}
