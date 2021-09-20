package com.simibubi.create.content.curiosities.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.IRegistryDelegate;

public class PotatoProjectileTypeManager {

	private static final Map<ResourceLocation, PotatoCannonProjectileType> BUILTIN_TYPE_MAP = new HashMap<>();
	private static final Map<ResourceLocation, PotatoCannonProjectileType> CUSTOM_TYPE_MAP = new HashMap<>();
	private static final Map<IRegistryDelegate<Item>, PotatoCannonProjectileType> ITEM_TO_TYPE_MAP = new HashMap<>();

	public static void registerBuiltinType(ResourceLocation id, PotatoCannonProjectileType type) {
		synchronized (BUILTIN_TYPE_MAP) {
			BUILTIN_TYPE_MAP.put(id, type);
		}
	}

	public static PotatoCannonProjectileType getBuiltinType(ResourceLocation id) {
		return BUILTIN_TYPE_MAP.get(id);
	}

	public static PotatoCannonProjectileType getCustomType(ResourceLocation id) {
		return CUSTOM_TYPE_MAP.get(id);
	}

	public static PotatoCannonProjectileType getTypeForItem(IRegistryDelegate<Item> item) {
		return ITEM_TO_TYPE_MAP.get(item);
	}

	public static Optional<PotatoCannonProjectileType> getTypeForStack(ItemStack item) {
		if (item.isEmpty())
			return Optional.empty();
		return Optional.ofNullable(getTypeForItem(item.getItem().delegate));
	}

	public static void clear() {
		CUSTOM_TYPE_MAP.clear();
		ITEM_TO_TYPE_MAP.clear();
	}

	public static void fillItemMap() {
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : BUILTIN_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (IRegistryDelegate<Item> delegate : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(delegate, type);
			}
		}
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : CUSTOM_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (IRegistryDelegate<Item> delegate : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(delegate, type);
			}
		}
		ITEM_TO_TYPE_MAP.remove(AllItems.POTATO_CANNON.get().delegate);
	}

	public static void toBuffer(PacketBuffer buffer) {
		buffer.writeVarInt(CUSTOM_TYPE_MAP.size());
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : CUSTOM_TYPE_MAP.entrySet()) {
			buffer.writeResourceLocation(entry.getKey());
			PotatoCannonProjectileType.toBuffer(entry.getValue(), buffer);
		}
	}

	public static void fromBuffer(PacketBuffer buffer) {
		clear();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			CUSTOM_TYPE_MAP.put(buffer.readResourceLocation(), PotatoCannonProjectileType.fromBuffer(buffer));
		}

		fillItemMap();
	}

	public static void syncTo(ServerPlayerEntity player) {
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SyncPacket());
	}

	public static void syncToAll() {
		AllPackets.channel.send(PacketDistributor.ALL.noArg(), new SyncPacket());
	}

	public static class ReloadListener extends JsonReloadListener {

		private static final Gson GSON = new Gson();

		public static final ReloadListener INSTANCE = new ReloadListener();

		protected ReloadListener() {
			super(GSON, "potato_cannon_projectile_types");
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
			clear();

			for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
				JsonElement element = entry.getValue();
				if (element.isJsonObject()) {
					ResourceLocation id = entry.getKey();
					JsonObject object = element.getAsJsonObject();
					PotatoCannonProjectileType type = PotatoCannonProjectileType.fromJson(object);
					CUSTOM_TYPE_MAP.put(id, type);
				}
			}

			fillItemMap();
		}

	}

	public static class SyncPacket extends SimplePacketBase {

		private PacketBuffer buffer;

		public SyncPacket() {
		}

		public SyncPacket(PacketBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void write(PacketBuffer buffer) {
			toBuffer(buffer);
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get().enqueueWork(() -> {
				fromBuffer(buffer);
			});
			context.get().setPacketHandled(true);
		}

	}

}
