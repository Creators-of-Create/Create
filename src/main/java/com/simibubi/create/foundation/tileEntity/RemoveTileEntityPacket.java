package com.simibubi.create.foundation.tileEntity;

import com.simibubi.create.foundation.networking.TileEntityDataPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class RemoveTileEntityPacket extends TileEntityDataPacket<SyncedTileEntity> {

	public RemoveTileEntityPacket(BlockPos pos) {
		super(pos);
	}

	public RemoveTileEntityPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeData(FriendlyByteBuf buffer) {}

	@Override
	protected void handlePacket(SyncedTileEntity tile) {
		if (!tile.hasLevel()) {
			tile.setRemoved();
			return;
		}

		tile.getLevel()
			.removeBlockEntity(tilePos);
	}

}
