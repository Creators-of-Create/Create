package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ConfigureMixerPacket extends TileEntityConfigurationPacket<MechanicalMixerTileEntity> {

	private int value;
	
	public ConfigureMixerPacket(BlockPos pos, int value) {
		super(pos);
		this.value = value;
	}
	
	public ConfigureMixerPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeInt(value);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		value = buffer.readInt();
	}

	@Override
	protected void applySettings(MechanicalMixerTileEntity te) {
		te.minIngredients = value;
		te.markDirty();
		te.sendData();
		te.checkBasin = true;
	}

}
