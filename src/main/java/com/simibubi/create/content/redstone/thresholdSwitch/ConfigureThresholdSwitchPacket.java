package com.simibubi.create.content.redstone.thresholdSwitch;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureThresholdSwitchPacket extends BlockEntityConfigurationPacket<ThresholdSwitchBlockEntity> {

	private int offBelow;
	private int onAbove;
	private boolean invert;
	private boolean inStacks;

	public ConfigureThresholdSwitchPacket(BlockPos pos, int offBelow, int onAbove, boolean invert, boolean inStacks) {
		super(pos);
		this.offBelow = offBelow;
		this.onAbove = onAbove;
		this.invert = invert;
		this.inStacks = inStacks;
	}

	public ConfigureThresholdSwitchPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		offBelow = buffer.readInt();
		onAbove = buffer.readInt();
		invert = buffer.readBoolean();
		inStacks = buffer.readBoolean();
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeInt(offBelow);
		buffer.writeInt(onAbove);
		buffer.writeBoolean(invert);
		buffer.writeBoolean(inStacks);
	}

	@Override
	protected void applySettings(ThresholdSwitchBlockEntity be) {
		be.offWhenBelow = offBelow;
		be.onWhenAbove = onAbove;
		be.setInverted(invert);
		be.setInStacks(inStacks);
	}

}
