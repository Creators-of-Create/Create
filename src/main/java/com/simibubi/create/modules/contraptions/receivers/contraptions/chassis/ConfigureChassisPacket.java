package com.simibubi.create.modules.contraptions.receivers.contraptions.chassis;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ConfigureChassisPacket extends TileEntityConfigurationPacket<ChassisTileEntity> {

	private int range;
	
	public ConfigureChassisPacket(BlockPos pos, int range) {
		super(pos);
		this.range = range;
	}
	
	public ConfigureChassisPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeInt(range);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		range = buffer.readInt();
	}

	@Override
	protected void applySettings(ChassisTileEntity te) {
		te.setRange(range);
		te.markDirty();
		te.sendData();
	}

}
