package com.simibubi.create.foundation.tileEntity;

import net.minecraft.core.BlockPos;

public interface IMultiTileContainer {
	
	public BlockPos getController();
	public boolean isController();
	public void setController(BlockPos pos);
	public BlockPos getLastKnownPos();

}
