package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ScrollValueUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

	int value;
	
	public ScrollValueUpdatePacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	public ScrollValueUpdatePacket(BlockPos pos, int amount) {
		super(pos);
		this.value = amount;
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeInt(value);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		value = buffer.readInt();
	}

	@Override
	protected void applySettings(SmartTileEntity te) {
		ScrollValueBehaviour behaviour = te.getBehaviour(ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.setValue(value);
	}

}
