package com.simibubi.create.content.contraptions.components.structureMovement.elevator;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ElevatorContactEditPacket extends TileEntityConfigurationPacket<ElevatorContactTileEntity> {

	private String shortName;
	private String longName;

	public ElevatorContactEditPacket(BlockPos pos, String shortName, String longName) {
		super(pos);
		this.shortName = shortName;
		this.longName = longName;
	}
	
	public ElevatorContactEditPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeUtf(shortName, 4);
		buffer.writeUtf(longName, 30);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		shortName = buffer.readUtf(4);
		longName = buffer.readUtf(30);
	}

	@Override
	protected void applySettings(ElevatorContactTileEntity te) {
		te.updateName(shortName, longName);
	}

}
