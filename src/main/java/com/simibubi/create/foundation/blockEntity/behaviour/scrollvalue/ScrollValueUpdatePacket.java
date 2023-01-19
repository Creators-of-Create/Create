package com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ScrollValueUpdatePacket extends BlockEntityConfigurationPacket<SmartBlockEntity> {

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
	protected void applySettings(SmartBlockEntity be) {
		ScrollValueBehaviour behaviour = be.getBehaviour(ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.setValue(value);
	}

}
