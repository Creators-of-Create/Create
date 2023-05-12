package com.simibubi.create.compat.computercraft;

import com.simibubi.create.foundation.networking.TileEntityDataPacket;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class AttachedComputerPacket extends TileEntityDataPacket<SyncedTileEntity> {

	private final boolean hasAttachedComputer;

	public AttachedComputerPacket(BlockPos tilePos, boolean hasAttachedComputer) {
		super(tilePos);
		this.hasAttachedComputer = hasAttachedComputer;
	}

	public AttachedComputerPacket(FriendlyByteBuf buffer) {
		super(buffer);
		this.hasAttachedComputer = buffer.readBoolean();
	}

	@Override
	protected void writeData(FriendlyByteBuf buffer) {
		buffer.writeBoolean(hasAttachedComputer);
	}

	@Override
	protected void handlePacket(SyncedTileEntity tile) {
		if (tile instanceof SmartTileEntity smartTile) {
			smartTile.getBehaviour(AbstractComputerBehaviour.TYPE)
				.setHasAttachedComputer(hasAttachedComputer);
		}
	}

}
