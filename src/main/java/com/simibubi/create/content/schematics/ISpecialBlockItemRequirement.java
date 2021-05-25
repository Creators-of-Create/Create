package com.simibubi.create.content.schematics;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public interface ISpecialBlockItemRequirement {

	default ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		return ItemRequirement.INVALID;
	}

}
