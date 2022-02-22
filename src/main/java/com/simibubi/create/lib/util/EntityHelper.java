package com.simibubi.create.lib.util;

import com.simibubi.create.Create;
import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.mixin.common.accessor.EntityAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class EntityHelper {
	public static final String EXTRA_DATA_KEY = "create_ExtraEntityData";
	public static final ResourceLocation EXTRA_DATA_PACKET = Create.asResource("extra_data");
	private static final Map<Integer, FriendlyByteBuf> QUEUED_DATA = new HashMap<>(); // can't guarantee that extra data is received after main packet

	/**
	 * Extra data sent from server to client.
	 */
	public static void handleDataSend(Entity entity, Consumer<Packet<?>> callback) {
		if (entity instanceof ExtraSpawnDataEntity extra) {
			FriendlyByteBuf data = PacketByteBufs.create();
			data.writeVarInt(entity.getId());
			extra.writeSpawnData(data);
			Packet<?> packet = ServerPlayNetworking.createS2CPacket(EXTRA_DATA_PACKET, data);
			callback.accept(packet);
		} else {
			Create.LOGGER.error("Tried to send extra data to an entity that doesn't implement ExtraSpawnDataEntity");
		}
	}

	/**
	 * Extra data received on client.
	 * If the entity exists, the main spawn packet was received first and data
	 * should be loaded immediately.
	 * If it does not exist yet, that means the extra data was received first.
	 * It will be stored in a map until the main spawn packet is received.
	 */
	@Environment(EnvType.CLIENT)
	public static void handleDataReceive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		int id = buf.readVarInt();
		FriendlyByteBuf copy = PacketByteBufs.copy(buf); // need to copy so the data survives client.execute()
		client.execute(() -> {
			Entity entity = handler.getLevel().getEntity(id);
			if (entity == null) {
				QUEUED_DATA.put(id, copy); // entity does not exist yet - save for later
			} else if (entity instanceof ExtraSpawnDataEntity extra) {
				extra.readSpawnData(copy); // entity exists - load now
				copy.release();
			} else {
				Create.LOGGER.error("Tried to receive extra data for an entity that doesn't implement ExtraSpawnDataEntity");
			}
		});
	}

	/**
	 * Invoked when main entity packet is received.
	 * data stored by handleDataReceive will be loaded here if present.
	 */
	public static void entityLoad(Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity extra) {
			int id = entity.getId();
			FriendlyByteBuf data = QUEUED_DATA.remove(id);
			if (data != null) {
				extra.readSpawnData(data);
				data.release();
			}
		} else {
			Create.LOGGER.error("Tried to receive extra data for an entity that doesn't implement ExtraSpawnDataEntity");
		}
	}

	@Environment(EnvType.CLIENT)
	public static void initClientPacketHandling() {
		ClientPlayNetworking.registerGlobalReceiver(EXTRA_DATA_PACKET, EntityHelper::handleDataReceive);
	}

	public static CompoundTag getExtraCustomData(Entity entity) {
		return ((EntityExtensions) entity).create$getExtraCustomData();
	}

	public static String getEntityString(Entity entity) {
		return ((EntityAccessor) entity).create$getEntityString();
	}

	private EntityHelper() {}
}
