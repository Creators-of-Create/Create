package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class NixieTubeTileEntity extends SyncedTileEntity {

	char tube1;
	char tube2;

	public NixieTubeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		tube1 = '0';
		tube2 = '0';
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		nbt.putInt("tube1", tube1);
		nbt.putInt("tube2", tube2);
		return nbt;
	}

	public void display(char tube1, char tube2) {
		this.tube1 = tube1;
		this.tube2 = tube2;
		markDirty();
		sendData();
	}

	@Override
	public void read(CompoundNBT nbt) {
		tube1 = (char) nbt.getInt("tube1");
		tube2 = (char) nbt.getInt("tube2");
		super.read(nbt);
	}

}
