package com.simibubi.create.compat.trainmap;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrainMapSyncRequestPacket extends SimplePacketBase {

	public TrainMapSyncRequestPacket() {}

	public TrainMapSyncRequestPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> TrainMapSync.requestReceived(context.getSender()));
		return true;
	}

}
