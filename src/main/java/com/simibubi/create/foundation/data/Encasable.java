package com.simibubi.create.foundation.data;

import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.relays.elementary.IEncased;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;

import com.simibubi.create.content.contraptions.relays.elementary.IEncasable;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Encasable {
	public static Map<Block, ArrayList<Block>> encasableBlocks = new HashMap<>();

	public static void register(IEncased encasedBlock, IEncasable blockToBeEncased, CasingBlock casing){
			if (!encasableBlocks.containsKey((Block) blockToBeEncased))
				encasableBlocks.put((Block) blockToBeEncased, new ArrayList<>());

			encasedBlock.setCasing(casing);
			encasableBlocks.get(blockToBeEncased).add((Block) encasedBlock);
	}
}
