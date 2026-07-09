package com.simibubi.create.compat.trainmap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.trainmap.TrainMapSync.TrainMapSyncEntry;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.UUIDUtil;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;

public class TrainMapSyncPacket implements ClientboundPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, TrainMapSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, packet -> packet.light,
			CatnipStreamCodecBuilders.list(Pair.streamCodec(UUIDUtil.STREAM_CODEC, TrainMapSyncEntry.STREAM_CODEC)), packet -> packet.entries,
			TrainMapSyncPacket::new
	);

	public boolean light;
	public List<Pair<UUID, TrainMapSyncEntry>> entries = new ArrayList<>();

	public TrainMapSyncPacket(boolean light) {
		this.light = light;
	}

	public TrainMapSyncPacket(boolean light, List<Pair<UUID, TrainMapSyncEntry>> entries) {
		this.light = light;
		this.entries = entries;
	}

	public void add(UUID trainId, TrainMapSyncEntry data) {
		entries.add(Pair.of(trainId, data));
	}

	@Override
	public void handle(Player player) {
		TrainMapSyncClient.receive(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRAIN_MAP_SYNC;
	}
}
