package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ConfigureMotorPacket extends TileEntityConfigurationPacket<MotorTileEntity> {

	private int speed;

	public ConfigureMotorPacket(BlockPos pos, int speed) {
		super(pos);
		this.speed = speed;
	}

	public ConfigureMotorPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeInt(speed);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		speed = buffer.readInt();
	}

	@Override
	protected void applySettings(MotorTileEntity te) {
		te.generatedSpeed = speed;
		te.updateGeneratedRotation();
	}

}
