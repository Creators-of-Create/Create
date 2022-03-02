package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.contraptions.processing.fan.transform.EntityTransformHelper;
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

@ParametersAreNonnullByDefault
public class CustomFanNetworkManager {

	public static final PreparableReloadListener FAN_TYPE = new SimpleJsonResourceReloadListener(new Gson(), "fan_processing_types") {

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profile) {
			TypeCustom.MAP.entrySet().removeIf(e -> e.getValue() instanceof TypeCustom);
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

	public static final PreparableReloadListener TRANSFORM_TYPE = new SimpleJsonResourceReloadListener(new Gson(), "fan_entity_transform") {

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profile) {
			EntityTransformHelper.LIST.removeIf(e -> e instanceof CustomTransformType);
			map.forEach((k, v) -> {
				CustomTransformConfig config = CustomTransformConfig.CODEC.decode(JsonOps.INSTANCE, v)
					.getOrThrow(false, LogManager.getLogger()::error).getFirst();
				try {
					new CustomTransformType(config);
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
			List<CustomFanTypeConfig> list_type = new ArrayList<>();
			TypeCustom.MAP.forEach((k, v) -> {
				if (v instanceof TypeCustom custom) {
					list_type.add(custom.getConfig());
				}
			});
			buffer.writeInt(list_type.size());
			for (CustomFanTypeConfig config : list_type) {
				Tag tag = CustomFanTypeConfig.CODEC.encodeStart(NbtOps.INSTANCE, config)
					.getOrThrow(false, LogManager.getLogger()::error);
				buffer.writeNbt((CompoundTag) tag);
			}
			List<CustomTransformConfig> list_transform = new ArrayList<>();
			EntityTransformHelper.LIST.forEach(e -> {
				if (e instanceof CustomTransformType custom) {
					list_transform.add(custom.config);
				}
			});
			buffer.writeInt(list_transform.size());
			for (CustomTransformConfig config : list_transform) {
				Tag tag = CustomTransformConfig.CODEC.encodeStart(NbtOps.INSTANCE, config)
					.getOrThrow(false, LogManager.getLogger()::error);
				buffer.writeNbt((CompoundTag) tag);
			}
		}

		@Override
		public void handle(Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				TypeCustom.MAP.entrySet().removeIf(e -> e.getValue() instanceof TypeCustom);
				EntityTransformHelper.LIST.removeIf(e -> e instanceof CustomTransformType);
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
				size = buffer.readInt();
				for (int i = 0; i < size; i++) {
					CompoundTag tag = buffer.readAnySizeNbt();
					CustomTransformConfig config = CustomTransformConfig.CODEC.decode(NbtOps.INSTANCE, tag)
						.getOrThrow(false, LogManager.getLogger()::error).getFirst();
					try {
						new CustomTransformType(config);
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
