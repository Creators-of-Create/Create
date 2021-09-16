package com.simibubi.create.content.logistics.packet;

import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateTileEntity;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class ConfigureFlexcratePacket extends TileEntityConfigurationPacket<AdjustableCrateTileEntity> {

	private int maxItems;
	
	public ConfigureFlexcratePacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	public ConfigureFlexcratePacket(BlockPos pos, int newMaxItems) {
		super(pos);
		this.maxItems = newMaxItems;
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeInt(maxItems);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		maxItems = buffer.readInt();
	}

	@Override
	protected void applySettings(AdjustableCrateTileEntity te) {
		te.allowedAmount = maxItems;
	}

}
