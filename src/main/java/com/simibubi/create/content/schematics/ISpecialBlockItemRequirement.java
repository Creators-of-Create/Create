package com.simibubi.create.content.schematics;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ISpecialBlockItemRequirement {

	default ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.INVALID;
	}

}
