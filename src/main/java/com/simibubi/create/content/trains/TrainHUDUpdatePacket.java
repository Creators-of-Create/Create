package com.simibubi.create.content.trains;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Function4;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class TrainHUDUpdatePacket implements CustomPacketPayload {
	protected final UUID trainId;

	@Nullable
	protected final Double throttle;
	protected final double speed;
	protected final int fuelTicks;

	private TrainHUDUpdatePacket(UUID trainId, @Nullable Double throttle, double speed, int fuelTicks) {
		this.trainId = trainId;
		this.throttle = throttle;
		this.speed = speed;
		this.fuelTicks = fuelTicks;
	}

	private static <T extends TrainHUDUpdatePacket> StreamCodec<ByteBuf, T> codec(Function4<UUID, Double, Double, Integer, T> factory) {
		return StreamCodec.composite(
				UUIDUtil.STREAM_CODEC, packet -> packet.trainId,
				CatnipStreamCodecBuilders.nullable(ByteBufCodecs.DOUBLE), packet -> packet.throttle,
				ByteBufCodecs.DOUBLE, packet -> packet.speed,
				ByteBufCodecs.VAR_INT, packet -> packet.fuelTicks,
				factory
		);
	}

	public static class Clientbound extends TrainHUDUpdatePacket implements ClientboundPacketPayload {
		public static final StreamCodec<ByteBuf, Clientbound> STREAM_CODEC = codec(Clientbound::new);

		public Clientbound(Train train) {
			this(train.id, train.throttle, nonStalledSpeed(train), train.fuelTicks);
		}

		private Clientbound(UUID trainId, @Nullable Double throttle, double speed, int fuelTicks) {
			super(trainId, throttle, speed, fuelTicks);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(LocalPlayer player) {
			Train train = Create.RAILWAYS.sided(null).trains.get(trainId);
			if (train == null)
				return;

			if (throttle != null) {
				train.throttle = throttle;
			}

			train.speed = speed;
			train.fuelTicks = fuelTicks;
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.S_TRAIN_HUD;
		}

		private static double nonStalledSpeed(Train train) {
			return train.speedBeforeStall == null ? train.speed : train.speedBeforeStall;
		}
	}

	public static class Serverbound extends TrainHUDUpdatePacket implements ServerboundPacketPayload {
		public static final StreamCodec<ByteBuf, Serverbound> STREAM_CODEC = codec(Serverbound::new);

		public Serverbound(Train train, Double sendThrottle) {
			this(train.id, sendThrottle, 0, 0);
		}

		private Serverbound(UUID trainId, @Nullable Double throttle, double speed, int fuelTicks) {
			super(trainId, throttle, speed, fuelTicks);
		}

		@Override
		public void handle(ServerPlayer player) {
			Train train = Create.RAILWAYS.sided(player.level()).trains.get(trainId);
			if (train == null)
				return;

			if (throttle != null)
				train.throttle = throttle;
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.C_TRAIN_HUD;
		}
	}

}
