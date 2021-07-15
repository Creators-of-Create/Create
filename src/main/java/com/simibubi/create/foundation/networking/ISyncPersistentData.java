package com.simibubi.create.foundation.networking;

import java.util.Iterator;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public interface ISyncPersistentData {

	void onPersistentDataUpdated();

	default void syncPersistentDataWithTracking(Entity self) {
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> self), new Packet(self));
	}

	public static class Packet extends SimplePacketBase {

		private int entityId;
		private Entity entity;
		private CompoundNBT readData;

		public Packet(Entity entity) {
			this.entity = entity;
			this.entityId = entity.getId();
		}

		public Packet(PacketBuffer buffer) {
			entityId = buffer.readInt();
			readData = buffer.readNbt();
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeInt(entityId);
			buffer.writeNbt(entity.getPersistentData());
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get()
					.enqueueWork(() -> {
						Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
						if (!(entityByID instanceof ISyncPersistentData))
							return;
						CompoundNBT data = entityByID.getPersistentData();
						for (Iterator<String> iterator = data.getAllKeys()
								.iterator(); iterator.hasNext(); ) {
							data.remove(iterator.next());
						}
						data.merge(readData);
						((ISyncPersistentData) entityByID).onPersistentDataUpdated();
					});
			context.get()
					.setPacketHandled(true);
		}

	}

}
