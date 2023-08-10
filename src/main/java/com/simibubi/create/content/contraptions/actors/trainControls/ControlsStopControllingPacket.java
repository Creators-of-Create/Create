package com.simibubi.create.content.contraptions.actors.trainControls;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class ControlsStopControllingPacket extends SimplePacketBase {

	public ControlsStopControllingPacket() {}

	public ControlsStopControllingPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(ControlsHandler::stopControlling);
		return true;
	}

}
