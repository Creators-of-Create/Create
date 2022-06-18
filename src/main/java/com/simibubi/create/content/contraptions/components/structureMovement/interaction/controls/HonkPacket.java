package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class HonkPacket extends SimplePacketBase {

	UUID trainId;
	boolean isHonk;

	public HonkPacket() {}

	public HonkPacket(Train train, boolean isHonk) {
		trainId = train.id;
		this.isHonk = isHonk;
	}

	public HonkPacket(FriendlyByteBuf buffer) {
		trainId = buffer.readUUID();
		isHonk = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(trainId);
		buffer.writeBoolean(isHonk);
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

			if (clientSide) {
				if (isHonk)
					train.honkTicks = train.honkTicks == 0 ? 20 : 13;
				else
					train.honkTicks = train.honkTicks > 5 ? 6 : 0;
			} else {
				AllAdvancements.TRAIN_WHISTLE.awardTo(sender);
				AllPackets.channel.send(PacketDistributor.ALL.noArg(), new HonkPacket(train, isHonk));
			}

		});
		c.setPacketHandled(true);
	}

	public static class Serverbound extends HonkPacket {

		public Serverbound(FriendlyByteBuf buffer) {
			super(buffer);
		}

		public Serverbound(Train train, boolean isHonk) {
			super(train, isHonk);
		}
	}

}
