package com.simibubi.create.content.curiosities.weapons;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class PotatoProjectileTypeManager {

	private static final Map<ResourceLocation, PotatoCannonProjectileType> BUILTIN_TYPE_MAP = new HashMap<>();
	private static final Map<ResourceLocation, PotatoCannonProjectileType> CUSTOM_TYPE_MAP = new HashMap<>();
	private static final Map<Item, PotatoCannonProjectileType> ITEM_TO_TYPE_MAP = new IdentityHashMap<>();

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

	public static PotatoCannonProjectileType getTypeForItem(Item item) {
		return ITEM_TO_TYPE_MAP.get(item);
	}

	public static Optional<PotatoCannonProjectileType> getTypeForStack(ItemStack item) {
		if (item.isEmpty())
			return Optional.empty();
		return Optional.ofNullable(getTypeForItem(item.getItem()));
	}

	public static void clear() {
		CUSTOM_TYPE_MAP.clear();
		ITEM_TO_TYPE_MAP.clear();
	}

	public static void fillItemMap() {
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : BUILTIN_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (Supplier<Item> delegate : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(delegate.get(), type);
			}
		}
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : CUSTOM_TYPE_MAP.entrySet()) {
			PotatoCannonProjectileType type = entry.getValue();
			for (Supplier<Item> delegate : type.getItems()) {
				ITEM_TO_TYPE_MAP.put(delegate.get(), type);
			}
		}
		ITEM_TO_TYPE_MAP.remove(AllItems.POTATO_CANNON.get());
	}

	public static void toBuffer(FriendlyByteBuf buffer) {
		buffer.writeVarInt(CUSTOM_TYPE_MAP.size());
		for (Map.Entry<ResourceLocation, PotatoCannonProjectileType> entry : CUSTOM_TYPE_MAP.entrySet()) {
			buffer.writeResourceLocation(entry.getKey());
			PotatoCannonProjectileType.toBuffer(entry.getValue(), buffer);
		}
	}

	public static void fromBuffer(FriendlyByteBuf buffer) {
		clear();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			CUSTOM_TYPE_MAP.put(buffer.readResourceLocation(), PotatoCannonProjectileType.fromBuffer(buffer));
		}

		fillItemMap();
	}

	public static void syncTo(ServerPlayer player) {
		AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new SyncPacket());
	}

	public static void syncToAll() {
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new SyncPacket());
	}

	public static class ReloadListener extends SimpleJsonResourceReloadListener {

		private static final Gson GSON = new Gson();

		public static final ReloadListener INSTANCE = new ReloadListener();

		protected ReloadListener() {
			super(GSON, "potato_cannon_projectile_types");
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
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

		private FriendlyByteBuf buffer;

		public SyncPacket() {
		}

		public SyncPacket(FriendlyByteBuf buffer) {
			this.buffer = buffer;
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			toBuffer(buffer);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> {
				fromBuffer(buffer);
			});
			return true;
		}

	}

}
