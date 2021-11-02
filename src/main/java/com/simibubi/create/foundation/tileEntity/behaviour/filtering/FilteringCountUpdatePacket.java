package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class FilteringCountUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

	int amount;
	
	public FilteringCountUpdatePacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	public FilteringCountUpdatePacket(BlockPos pos, int amount) {
		super(pos);
		this.amount = amount;
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeInt(amount);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		amount = buffer.readInt();
	}

	@Override
	protected void applySettings(SmartTileEntity te) {
		FilteringBehaviour behaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.forceClientState = true;
		behaviour.count = amount;
		te.setChanged();
		te.sendData();
	}

}
