package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public class PersistantDataPacketRequest extends SimplePacketBase {

	int entityId;

	public PersistantDataPacketRequest(Entity entity) {
		entityId = entity.getEntityId();
	}

	public PersistantDataPacketRequest(PacketBuffer buffer) {
		entityId = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(entityId);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null || sender.world == null)
					return;
				Entity entityByID = sender.world.getEntityByID(entityId);
				if (entityByID == null)
					return;
				AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> sender),
					new PersistantDataPacket(entityByID));
			});
		context.get()
			.setPacketHandled(true);
	}

}