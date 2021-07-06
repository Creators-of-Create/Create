package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.events.CommonEvents;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class LeftClickPacket extends SimplePacketBase {

	public LeftClickPacket() {}

	LeftClickPacket(PacketBuffer buffer) {}

	@Override
	public void write(PacketBuffer buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		if (ctx.getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;
		ctx.enqueueWork(() -> CommonEvents.leftClickEmpty(ctx.getSender()));
		ctx.setPacketHandled(true);
	}

}
