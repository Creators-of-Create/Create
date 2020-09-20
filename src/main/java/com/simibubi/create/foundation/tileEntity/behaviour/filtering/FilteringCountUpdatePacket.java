package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class FilteringCountUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

	int amount;
	
	public FilteringCountUpdatePacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public FilteringCountUpdatePacket(BlockPos pos, int amount) {
		super(pos);
		this.amount = amount;
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeInt(amount);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		amount = buffer.readInt();
	}

	@Override
	protected void applySettings(SmartTileEntity te) {
		FilteringBehaviour behaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.forceClientState = true;
		behaviour.count = amount;
		te.markDirty();
		te.sendData();
	}

}
