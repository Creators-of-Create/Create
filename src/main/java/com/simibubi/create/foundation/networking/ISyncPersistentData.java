package com.simibubi.create.foundation.networking;

import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
		private CompoundTag readData;

		public Packet(Entity entity) {
			this.entity = entity;
			this.entityId = entity.getId();
		}

		public Packet(FriendlyByteBuf buffer) {
			entityId = buffer.readInt();
			readData = buffer.readNbt();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeInt(entityId);
			buffer.writeNbt(entity.getPersistentData());
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get()
				.enqueueWork(() -> {
					Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
					CompoundTag data = entityByID.getPersistentData();
					new HashSet<>(data.getAllKeys()).forEach(data::remove);
					data.merge(readData);
					if (!(entityByID instanceof ISyncPersistentData))
						return;
					((ISyncPersistentData) entityByID).onPersistentDataUpdated();
				});
			context.get()
				.setPacketHandled(true);
		}

	}

}
