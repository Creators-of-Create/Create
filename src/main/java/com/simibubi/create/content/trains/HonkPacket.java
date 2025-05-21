package com.simibubi.create.content.trains;

import java.util.UUID;
import java.util.function.BiFunction;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class HonkPacket implements CustomPacketPayload {
	protected final UUID trainId;
	protected final boolean isHonk;

	private HonkPacket(UUID trainId, boolean isHonk) {
		this.trainId = trainId;
		this.isHonk = isHonk;
	}

	private static <T extends HonkPacket> StreamCodec<ByteBuf, T> codec(BiFunction<UUID, Boolean, T> factory) {
		return StreamCodec.composite(
				UUIDUtil.STREAM_CODEC, packet -> packet.trainId,
				ByteBufCodecs.BOOL, packet -> packet.isHonk,
				factory
		);
	}

	public static class Clientbound extends HonkPacket implements ClientboundPacketPayload {
		public static final StreamCodec<ByteBuf, Clientbound> STREAM_CODEC = codec(Clientbound::new);

		public Clientbound(Train train, boolean isHonk) {
			this(train.id, isHonk);
		}

		private Clientbound(UUID id, boolean isHonk) {
			super(id, isHonk);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(LocalPlayer player) {
			Train train = Create.RAILWAYS.sided(null).trains.get(trainId);
			if (train == null)
				return;

			if (isHonk)
				train.honkTicks = train.honkTicks == 0 ? 20 : 13;
			else
				train.honkTicks = train.honkTicks > 5 ? 6 : 0;
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.S_TRAIN_HONK;
		}
	}

	public static class Serverbound extends HonkPacket implements ServerboundPacketPayload {
		public static final StreamCodec<ByteBuf, Serverbound> STREAM_CODEC = codec(Serverbound::new);

		public Serverbound(Train train, boolean isHonk) {
			this(train.id, isHonk);
		}

		private Serverbound(UUID id, boolean isHonk) {
			super(id, isHonk);
		}

		@Override
		public void handle(ServerPlayer player) {
			Train train = Create.RAILWAYS.sided(player.level()).trains.get(trainId);
			if (train == null)
				return;

			AllAdvancements.TRAIN_WHISTLE.awardTo(player);
			CatnipServices.NETWORK.sendToAllClients(new HonkPacket.Clientbound(train, isHonk));
			
			Entity entity = train.carriages.get(0).anyAvailableEntity();
			if (entity == null) entity = player;

			player.level().gameEvent(entity, GameEvent.RESONATE_15, player.position());
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.C_TRAIN_HONK;
		}
	}

}
