package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.events.CommonEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;

public class LeftClickPacket extends SimplePacketBase {

	public LeftClickPacket() {}

	LeftClickPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		if (ctx.getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;
		ctx.enqueueWork(() -> CommonEvents.leftClickEmpty(ctx.getSender()));
		ctx.setPacketHandled(true);
	}

}
