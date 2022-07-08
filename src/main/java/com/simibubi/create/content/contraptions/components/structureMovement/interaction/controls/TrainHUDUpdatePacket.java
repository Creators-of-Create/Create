package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrainHUDUpdatePacket extends SimplePacketBase {

	UUID trainId;

	Double throttle;
	double speed;
	int fuelTicks;

	public TrainHUDUpdatePacket() {}

	public TrainHUDUpdatePacket(Train train) {
		trainId = train.id;
		throttle = train.throttle;
		speed = train.speedBeforeStall == null ? train.speed : train.speedBeforeStall;
		fuelTicks = train.fuelTicks;
	}

	public TrainHUDUpdatePacket(FriendlyByteBuf buffer) {
		trainId = buffer.readUUID();
		if (buffer.readBoolean())
			throttle = buffer.readDouble();
		speed = buffer.readDouble();
		fuelTicks = buffer.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(trainId);
		buffer.writeBoolean(throttle != null);
		if (throttle != null)
			buffer.writeDouble(throttle);
		buffer.writeDouble(speed);
		buffer.writeInt(fuelTicks);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context c = context.get();
		c.enqueueWork(() -> {
			ServerPlayer sender = c.getSender();
			boolean clientSide = sender == null;
			Train train = Create.RAILWAYS.sided(clientSide ? null : sender.level).trains.get(trainId);
			if (train == null)
				return;

			if (throttle != null)
				train.throttle = throttle;
			if (clientSide) {
				train.speed = speed;
				train.fuelTicks = fuelTicks;
			}

		});
		c.setPacketHandled(true);
	}

	public static class Serverbound extends TrainHUDUpdatePacket {

		public Serverbound(FriendlyByteBuf buffer) {
			super(buffer);
		}

		public Serverbound(Train train, Double sendThrottle) {
			trainId = train.id;
			throttle = sendThrottle;
		}
	}

}
