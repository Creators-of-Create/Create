package com.simibubi.create.compat.trainmap;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class TrainMapSyncRequestPacket implements ServerboundPacketPayload {
	public static final TrainMapSyncRequestPacket INSTANCE = new TrainMapSyncRequestPacket();
	public static final StreamCodec<ByteBuf, TrainMapSyncRequestPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public void handle(ServerPlayer player) {
		TrainMapSync.requestReceived(player);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRAIN_MAP_REQUEST;
	}
}
