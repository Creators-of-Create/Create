package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class ControlsStopControllingPacket extends SimplePacketBase {

	public ControlsStopControllingPacket() {}

	public ControlsStopControllingPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(ControlsHandler::stopControlling);
		context.get()
			.setPacketHandled(true);
	}

}
