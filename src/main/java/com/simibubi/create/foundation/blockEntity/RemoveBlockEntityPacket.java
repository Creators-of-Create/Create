package com.simibubi.create.foundation.blockEntity;

import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class RemoveBlockEntityPacket extends BlockEntityDataPacket<SyncedBlockEntity> {

	public RemoveBlockEntityPacket(BlockPos pos) {
		super(pos);
	}

	public RemoveBlockEntityPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeData(FriendlyByteBuf buffer) {}

	@Override
	protected void handlePacket(SyncedBlockEntity be) {
		if (!be.hasLevel()) {
			be.setRemoved();
			return;
		}

		be.getLevel()
			.removeBlockEntity(pos);
	}

}
