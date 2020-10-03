package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		fromTag(state, tag);
	}

	public void sendData() {
		if (world != null)
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (world != null)
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 1);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 1, writeToClient(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readClientUpdate(getBlockState(), pkt.getNbtCompound());
	}

	// Special handling for client update packets
	public void readClientUpdate(BlockState state, CompoundNBT tag) {
		fromTag(state, tag);
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
