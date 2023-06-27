package com.simibubi.create.content.contraptions.minecart;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(id1);
		buffer.writeInt(id2);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender != null)
				CouplingHandler.tryToCoupleCarts(sender, sender.level, id1, id2);
		});
		return true;
	}

}
