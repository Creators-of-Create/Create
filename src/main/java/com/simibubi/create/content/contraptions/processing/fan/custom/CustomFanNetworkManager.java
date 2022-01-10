package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CustomFanNetworkManager {

	public static final PreparableReloadListener LISTENER = new SimpleJsonResourceReloadListener(new Gson(), "fan_processing_types") {

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profile) {
			TypeCustom.onDataReload();
			map.forEach((k, v) -> {
				CustomFanTypeConfig config = CustomFanTypeConfig.CODEC.decode(JsonOps.INSTANCE, v)
						.getOrThrow(false, LogManager.getLogger()::error).getFirst();
				try {
					new TypeCustom(config);
				} catch (IllegalArgumentException e) {
					LogManager.getLogger().error(e);
				}
			});
		}
	};

	public static class SyncPacket extends SimplePacketBase {

		private FriendlyByteBuf buffer;

		public SyncPacket() {
		}

		public SyncPacket(FriendlyByteBuf buffer) {
			this.buffer = buffer;
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			List<CustomFanTypeConfig> list = new ArrayList<>();
			TypeCustom.MAP.forEach((k, v) -> {
				if (v instanceof TypeCustom custom) {
					list.add(custom.getConfig());
				}
			});
			buffer.writeInt(list.size());
			for (CustomFanTypeConfig config : list) {
				Tag tag = CustomFanTypeConfig.CODEC.encode(config, NbtOps.INSTANCE, NbtOps.INSTANCE.empty())
						.getOrThrow(false, LogManager.getLogger()::error);
				buffer.writeNbt((CompoundTag) tag);
			}
		}

		@Override
		public void handle(Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				TypeCustom.onDataReload();
				int size = buffer.readInt();
				for (int i = 0; i < size; i++) {
					CompoundTag tag = buffer.readAnySizeNbt();
					CustomFanTypeConfig config = CustomFanTypeConfig.CODEC.decode(NbtOps.INSTANCE, tag)
							.getOrThrow(false, LogManager.getLogger()::error).getFirst();
					try {
						new TypeCustom(config);
					} catch (IllegalArgumentException e) {
						LogManager.getLogger().error(e);
					}
				}
			});
			context.get().setPacketHandled(true);
		}
	}

	public static void onDatapackSync(OnDatapackSyncEvent event) {
		AllPackets.channel.send(event.getPlayer() == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(event::getPlayer), new SyncPacket());
	}


}
