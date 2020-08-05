package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MinecartCouplingCreationPacket extends SimplePacketBase {

	int id1, id2;

	public MinecartCouplingCreationPacket(AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
		id1 = cart1.getEntityId();
		id2 = cart2.getEntityId();
	}

	public MinecartCouplingCreationPacket(PacketBuffer buffer) {
		id1 = buffer.readInt();
		id2 = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(id1);
		buffer.writeInt(id2);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender != null)
					MinecartCouplingHandler.connectCarts(sender, sender.world, id1, id2);
			});
		context.get()
			.setPacketHandled(true);
	}

}