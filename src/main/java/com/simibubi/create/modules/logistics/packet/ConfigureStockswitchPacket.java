package com.simibubi.create.modules.logistics.packet;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;
import com.simibubi.create.modules.logistics.block.StockpileSwitchTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ConfigureStockswitchPacket extends TileEntityConfigurationPacket<StockpileSwitchTileEntity> {

	private float offBelow;
	private float onAbove;
	
	public ConfigureStockswitchPacket(BlockPos pos, float offBelow, float onAbove) {
		super(pos);
		this.offBelow = offBelow;
		this.onAbove = onAbove;
	}
	
	public ConfigureStockswitchPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	@Override
	protected void readSettings(PacketBuffer buffer) {
		offBelow = buffer.readFloat();
		onAbove = buffer.readFloat();
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeFloat(offBelow);
		buffer.writeFloat(onAbove);
	}

	@Override
	protected void applySettings(StockpileSwitchTileEntity te) {
		te.offWhenBelow = offBelow;
		te.onWhenAbove = onAbove;
	}
	
}
