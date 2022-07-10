package com.simibubi.create.content.schematics;

import net.minecraft.world.level.block.state.BlockState;

public interface ISpecialBlockEntityItemRequirement {

	public ItemRequirement getRequiredItems(BlockState state);

}
