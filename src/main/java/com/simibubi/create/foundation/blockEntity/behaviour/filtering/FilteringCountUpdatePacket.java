package com.simibubi.create.foundation.blockEntity.behaviour.filtering;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class FilteringCountUpdatePacket extends BlockEntityConfigurationPacket<SmartBlockEntity> {

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
	protected void applySettings(SmartBlockEntity be) {
		FilteringBehaviour behaviour = be.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.forceClientState = true;
		behaviour.count = amount;
		be.setChanged();
		be.sendData();
	}

}
