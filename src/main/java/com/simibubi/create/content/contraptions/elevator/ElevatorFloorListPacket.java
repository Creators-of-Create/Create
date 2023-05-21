package com.simibubi.create.content.contraptions.elevator;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class ElevatorFloorListPacket extends SimplePacketBase {

	private int entityId;
	private List<IntAttached<Couple<String>>> floorsList;

	public ElevatorFloorListPacket(AbstractContraptionEntity entity, List<IntAttached<Couple<String>>> floorsList) {
		this.entityId = entity.getId();
		this.floorsList = floorsList;
	}

	public ElevatorFloorListPacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		int size = buffer.readInt();
		floorsList = new ArrayList<>();
		for (int i = 0; i < size; i++)
			floorsList.add(IntAttached.with(buffer.readInt(), Couple.create(buffer.readUtf(), buffer.readUtf())));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeInt(floorsList.size());
		for (IntAttached<Couple<String>> entry : floorsList) {
			buffer.writeInt(entry.getFirst());
			entry.getSecond()
				.forEach(buffer::writeUtf);
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
			if (!(entityByID instanceof AbstractContraptionEntity ace))
				return;
			if (!(ace.getContraption()instanceof ElevatorContraption ec))
				return;

			ec.namesList = floorsList;
			ec.syncControlDisplays();
		});
		return true;
	}

	public static class RequestFloorList extends SimplePacketBase {

		private int entityId;

		public RequestFloorList(AbstractContraptionEntity entity) {
			this.entityId = entity.getId();
		}

		public RequestFloorList(FriendlyByteBuf buffer) {
			entityId = buffer.readInt();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeInt(entityId);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> {
				ServerPlayer sender = context.getSender();
				Entity entityByID = sender.getLevel()
					.getEntity(entityId);
				if (!(entityByID instanceof AbstractContraptionEntity ace))
					return;
				if (!(ace.getContraption()instanceof ElevatorContraption ec))
					return;
				AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> sender),
					new ElevatorFloorListPacket(ace, ec.namesList));
			});
			return true;
		}

	}

}
