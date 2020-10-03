package com.simibubi.create.content.logistics.packet;

import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

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
	
	public ConfigureStockswitchPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	@Override
	protected void readSettings(PacketBuffer buffer) {
		offBelow = buffer.readFloat();
		onAbove = buffer.readFloat();
		invert = buffer.readBoolean();
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
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
