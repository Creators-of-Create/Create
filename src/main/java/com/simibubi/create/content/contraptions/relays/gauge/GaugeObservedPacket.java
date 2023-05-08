package com.simibubi.create.content.contraptions.relays.gauge;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class GaugeObservedPacket extends BlockEntityConfigurationPacket<StressGaugeBlockEntity> {

	public GaugeObservedPacket(BlockPos pos) {
		super(pos);
	}

	public GaugeObservedPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(StressGaugeBlockEntity be) {
		be.onObserved();
	}
	
	@Override
	protected boolean causeUpdate() {
		return false;
	}

}
