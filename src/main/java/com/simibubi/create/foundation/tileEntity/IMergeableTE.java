package com.simibubi.create.foundation.tileEntity;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMergeableTE {
	
	public void accept(BlockEntity other);

}
