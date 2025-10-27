package com.simibubi.create.content.trains.entity;

import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.CreateClient;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

public record RemoveTrainPacket(UUID id) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, RemoveTrainPacket> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(RemoveTrainPacket::new, RemoveTrainPacket::id);

	public RemoveTrainPacket(Train train) {
		this(train.id);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		CreateClient.RAILWAYS.trains.remove(this.id);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.REMOVE_TRAIN;
	}
}
