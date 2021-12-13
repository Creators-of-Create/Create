package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public class CouplingCreationPacket extends SimplePacketBase {

	int id1, id2;

	public CouplingCreationPacket(AbstractMinecart cart1, AbstractMinecart cart2) {
		id1 = cart1.getId();
		id2 = cart2.getId();
	}

	public CouplingCreationPacket(FriendlyByteBuf buffer) {
		id1 = buffer.readInt();
		id2 = buffer.readInt();
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(id1);
		buffer.writeInt(id2);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer sender = context.get()
					.getSender();
				if (sender != null)
					CouplingHandler.tryToCoupleCarts(sender, sender.level, id1, id2);
			});
		context.get()
			.setPacketHandled(true);
	}

}
