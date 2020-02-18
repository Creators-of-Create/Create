package com.simibubi.create.foundation.behaviour.scrollvalue;

import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ScrollValueUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

	int value;
	
	public ScrollValueUpdatePacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public ScrollValueUpdatePacket(BlockPos pos, int amount) {
		super(pos);
		this.value = amount;
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
	protected void applySettings(SmartTileEntity te) {
		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(te, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.setValue(value);
	}

}
