package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

public enum CartAssembleRailType implements IStringSerializable {
	
	REGULAR(Blocks.RAIL),
	POWERED_RAIL(Blocks.POWERED_RAIL),
	DETECTOR_RAIL(Blocks.DETECTOR_RAIL),
	ACTIVATOR_RAIL(Blocks.ACTIVATOR_RAIL),
	
	;

	public Block railBlock;
	public Item railItem;

	private CartAssembleRailType(Block block) {
		this.railBlock = block;
		this.railItem = block.asItem();
	}
	
	@Override
	public String getName() {
		return Lang.asId(name());
	}

}
