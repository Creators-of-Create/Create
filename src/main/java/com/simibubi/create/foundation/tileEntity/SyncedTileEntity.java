package com.simibubi.create.foundation.tileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class SyncedTileEntity extends TileEntity {

	public SyncedTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public CompoundNBT getTileData() {
		return super.getTileData();
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}

	public void sendData() {
		world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 1);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 1, writeToClient(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readClientUpdate(pkt.getNbtCompound());
	}

	// Special handling for client update packets
	public void readClientUpdate(CompoundNBT tag) {
		read(tag);
	}

	// Special handling for client update packets
	public CompoundNBT writeToClient(CompoundNBT tag) {
		return write(tag);
	}
	
	public void notifyUpdate() {
		markDirty();
		sendData();
	}

}
