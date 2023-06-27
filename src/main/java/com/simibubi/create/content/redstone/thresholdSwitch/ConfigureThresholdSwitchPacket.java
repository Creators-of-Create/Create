package com.simibubi.create.content.redstone.thresholdSwitch;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureThresholdSwitchPacket extends BlockEntityConfigurationPacket<ThresholdSwitchBlockEntity> {

	private float offBelow;
	private float onAbove;
	private boolean invert;

	public ConfigureThresholdSwitchPacket(BlockPos pos, float offBelow, float onAbove, boolean invert) {
		super(pos);
		this.offBelow = offBelow;
		this.onAbove = onAbove;
		this.invert = invert;
	}
	
	public ConfigureThresholdSwitchPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		offBelow = buffer.readFloat();
		onAbove = buffer.readFloat();
		invert = buffer.readBoolean();
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeFloat(offBelow);
		buffer.writeFloat(onAbove);
		buffer.writeBoolean(invert);
	}

	@Override
	protected void applySettings(ThresholdSwitchBlockEntity be) {
		be.offWhenBelow = offBelow;
		be.onWhenAbove = onAbove;
		be.setInverted(invert);
	}
	
}
