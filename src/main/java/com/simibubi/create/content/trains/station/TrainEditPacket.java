package com.simibubi.create.content.trains.station;

import java.util.UUID;

import com.mojang.datafixers.util.Function4;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class TrainEditPacket implements CustomPacketPayload {
	protected final UUID id;
	protected final String name;
	protected final ResourceLocation iconType;
	protected final int mapColor;

	protected TrainEditPacket(UUID id, String name, ResourceLocation iconType, int mapColor) {
		this.id = id;
		this.name = name;
		this.iconType = iconType;
		this.mapColor = mapColor;
	}

	private static <T extends TrainEditPacket> StreamCodec<ByteBuf, T> codec(Function4<UUID, String, ResourceLocation, Integer, T> factory) {
		return StreamCodec.composite(
				UUIDUtil.STREAM_CODEC, packet -> packet.id,
				ByteBufCodecs.stringUtf8(256), packet -> packet.name,
				ResourceLocation.STREAM_CODEC, packet -> packet.iconType,
				ByteBufCodecs.INT, packet -> packet.mapColor,
				factory
		);
	}
	
	public void handleSided(Player sender) {
		Level level = sender == null ? null : sender.level();
		Train train = Create.RAILWAYS.sided(level).trains.get(id);
		if (train == null)
			return;
		if (!name.isBlank()) {
			train.name = Component.literal(name);
		}
		train.icon = TrainIconType.byId(iconType);
		train.mapColorIndex = mapColor;
		if (sender != null)
			CatnipServices.NETWORK.sendToAllClients(new TrainEditReturnPacket(id, name, iconType, mapColor));
	}

	public static class Serverbound extends TrainEditPacket implements ServerboundPacketPayload {
		public static final StreamCodec<ByteBuf, Serverbound> STREAM_CODEC = codec(Serverbound::new);

		public Serverbound(UUID id, String name, ResourceLocation iconType, int mapColor) {
			super(id, name, iconType, mapColor);
		}

		@Override
		public void handle(ServerPlayer sender) {
			handleSided(sender);
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.C_CONFIGURE_TRAIN;
		}
	}

	public static class TrainEditReturnPacket extends TrainEditPacket implements ClientboundPacketPayload {
		public static final StreamCodec<ByteBuf, TrainEditReturnPacket> STREAM_CODEC = codec(TrainEditReturnPacket::new);

		public TrainEditReturnPacket(UUID id, String name, ResourceLocation iconType,  int mapColor) {
			super(id, name, iconType, mapColor);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(LocalPlayer player) {
			handleSided(null);
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.S_CONFIGURE_TRAIN;
		}
	}

}
