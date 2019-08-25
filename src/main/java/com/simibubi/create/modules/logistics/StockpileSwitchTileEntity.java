package com.simibubi.create.modules.logistics;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;

public class StockpileSwitchTileEntity extends SyncedTileEntity {

	private float offWhenAbove;
	private float onWhenBelow;
//	private float currentLevel;
	
	public StockpileSwitchTileEntity() {
		super(AllTileEntities.STOCKPILE_SWITCH.type);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		
		offWhenAbove = compound.getFloat("OffAbove");
		onWhenBelow = compound.getFloat("OnBelow");
		updateCurrentLevel();
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putFloat("OffAbove", offWhenAbove);
		compound.putFloat("OnBelow", onWhenBelow);
		
		return super.write(compound);
	}
	
	private void updateCurrentLevel() {
		
	}

}
