package com.simibubi.create.content.logistics.packet;

import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureStockswitchPacket extends TileEntityConfigurationPacket<StockpileSwitchTileEntity> {

	private float offBelow;
	private float onAbove;
	private boolean invert;
	
	public ConfigureStockswitchPacket(BlockPos pos, float offBelow, float onAbove, boolean invert) {
		super(pos);
		this.offBelow = offBelow;
		this.onAbove = onAbove;
		this.invert = invert;
	}
	
	public ConfigureStockswitchPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		offBelow = buffer.readFloat();
		onAbove = buffer.readFloat();
		invert = buffer.readBoolean();
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeFloat(offBelow);
		buffer.writeFloat(onAbove);
		buffer.writeBoolean(invert);
	}

	@Override
	protected void applySettings(StockpileSwitchTileEntity te) {
		te.offWhenBelow = offBelow;
		te.onWhenAbove = onAbove;
		te.setInverted(invert);
	}
	
}
