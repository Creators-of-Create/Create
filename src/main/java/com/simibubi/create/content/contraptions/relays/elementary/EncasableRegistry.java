package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.Block;

public class EncasableRegistry {

	public static Map<Block, List<Block>> encasableBlocks = new HashMap<>();

	public static void register(Encased encasedBlock, Encasable blockToBeEncased, Block casing) {
		if (!encasableBlocks.containsKey((Block) blockToBeEncased))
			encasableBlocks.put((Block) blockToBeEncased, new ArrayList<>(2));

		encasedBlock.setCasing(casing);
		encasableBlocks.get(blockToBeEncased).add((Block) encasedBlock);
	}

	public static List<Block> getValidEncasedBlocks(Block block){ return encasableBlocks.get(block); }
}
