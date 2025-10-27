package com.simibubi.create.content.logistics.tableCloth;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ShopUpdatePacket extends BlockEntityDataPacket<TableClothBlockEntity> {

	public ShopUpdatePacket(BlockPos pos) {
		super(pos);
	}

	public ShopUpdatePacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeData(FriendlyByteBuf buffer) {
	}

	@Override
	protected void handlePacket(TableClothBlockEntity be) {
		if (!be.hasLevel()) {
			return;
		}

		be.invalidateItemsForRender();
	}

}
