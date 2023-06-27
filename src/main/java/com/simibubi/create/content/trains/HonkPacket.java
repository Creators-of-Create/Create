package com.simibubi.create.content.trains;

import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.advancement.AllAdvancements;
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
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			boolean clientSide = sender == null;
			Train train = Create.RAILWAYS.sided(clientSide ? null : sender.level()).trains.get(trainId);
			if (train == null)
				return;

			if (clientSide) {
				if (isHonk)
					train.honkTicks = train.honkTicks == 0 ? 20 : 13;
				else
					train.honkTicks = train.honkTicks > 5 ? 6 : 0;
			} else {
				AllAdvancements.TRAIN_WHISTLE.awardTo(sender);
				AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new HonkPacket(train, isHonk));
			}

		});
		return true;
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
