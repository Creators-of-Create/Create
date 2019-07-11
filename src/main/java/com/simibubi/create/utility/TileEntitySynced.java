package com.simibubi.create.utility;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class TileEntitySynced extends TileEntity {

	public TileEntitySynced(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}
	
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket(){
	    return new SUpdateTileEntityPacket(getPos(), 1, write(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
	    read(pkt.getNbtCompound());
	}

}
