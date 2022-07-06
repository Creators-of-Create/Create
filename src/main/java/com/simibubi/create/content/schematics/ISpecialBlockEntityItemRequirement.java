package com.simibubi.create.content.schematics;

import net.minecraft.world.level.block.state.BlockState;

public interface ISpecialBlockEntityItemRequirement {

	default ItemRequirement getRequiredItems(BlockState state) {
		return ItemRequirement.INVALID;
	}

}
