package com.simibubi.create.foundation.blockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMergeableBE {
	
	public void accept(BlockEntity other);

}
