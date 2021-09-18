package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.events.CommonEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class LeftClickPacket extends SimplePacketBase {

	public LeftClickPacket() {}

	LeftClickPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public void handle(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		if (ctx.getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;
		ctx.enqueueWork(() -> CommonEvents.leftClickEmpty(ctx.getSender()));
		ctx.setPacketHandled(true);
	}

}
