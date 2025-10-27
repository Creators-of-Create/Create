package com.simibubi.create.content.trains.signal;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllPackets;
import com.simibubi.create.CreateClient;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record SignalEdgeGroupPacket(List<UUID> ids, List<EdgeGroupColor> colors, boolean add) implements ClientboundPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, SignalEdgeGroupPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC), p -> p.ids,
			CatnipStreamCodecBuilders.list(EdgeGroupColor.STREAM_CODEC), p -> p.colors,
			ByteBufCodecs.BOOL, p -> p.add,
	        SignalEdgeGroupPacket::new
	);

	public SignalEdgeGroupPacket(UUID id, EdgeGroupColor color) {
		this(ImmutableList.of(id), ImmutableList.of(color), true);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Map<UUID, SignalEdgeGroup> signalEdgeGroups = CreateClient.RAILWAYS.signalEdgeGroups;
		for (int i = 0; i < ids.size(); i++) {
			UUID id = ids.get(i);
			if (!add) {
				signalEdgeGroups.remove(id);
				continue;
			}

			SignalEdgeGroup group = new SignalEdgeGroup(id);
			signalEdgeGroups.put(id, group);
			if (i < colors.size())
				group.color = colors.get(i);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.SYNC_EDGE_GROUP;
	}
}
