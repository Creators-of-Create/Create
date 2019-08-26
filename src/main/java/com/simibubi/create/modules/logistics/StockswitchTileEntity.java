package com.simibubi.create.modules.logistics;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class StockswitchTileEntity extends SyncedTileEntity {

	float onWhenAbove;
	float offWhenBelow;
	float currentLevel;
	
	public StockswitchTileEntity() {
		this(AllTileEntities.STOCKSWITCH.type);
	}
	
	public StockswitchTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		onWhenAbove = .75f;
		offWhenBelow = .25f;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		
		onWhenAbove = compound.getFloat("OnAbove");
		offWhenBelow = compound.getFloat("OffBelow");
		updateCurrentLevel();
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putFloat("OnAbove", onWhenAbove);
		compound.putFloat("OffBelow", offWhenBelow);
		
		return super.write(compound);
	}
	
	private void updateCurrentLevel() {
		
	}

}
