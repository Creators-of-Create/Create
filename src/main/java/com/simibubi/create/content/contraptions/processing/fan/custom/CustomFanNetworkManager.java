package com.simibubi.create.content.contraptions.processing.fan.custom;

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

import org.apache.logging.log4j.LogManager;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class CustomFanNetworkManager {

	public static final PreparableReloadListener FAN_TYPE = new SimpleJsonResourceReloadListener(new Gson(), "fan_processing_types") {

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profile) {
			TypeCustom.MAP.entrySet().removeIf(e -> e.getValue() instanceof TypeCustom);
			map.forEach((k, v) -> {
				try {
					CustomFanTypeConfig config = CustomFanTypeConfig.CODEC.decode(JsonOps.INSTANCE, v)
							.getOrThrow(false, LogManager.getLogger()::error).getFirst();
					new TypeCustom(k, config);
				} catch (IllegalArgumentException e) {
					LogManager.getLogger().throwing(e);
				}
			});
		}
	};

	public static final PreparableReloadListener TRANSFORM_TYPE = new SimpleJsonResourceReloadListener(new Gson(), "fan_entity_transform") {

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profile) {
			EntityTransformHelper.LIST.removeIf(e -> e instanceof CustomTransformType);
			map.forEach((k, v) -> {
				try {
					CustomTransformConfig config = CustomTransformConfig.CODEC.decode(JsonOps.INSTANCE, v)
							.getOrThrow(false, LogManager.getLogger()::error).getFirst();
					new CustomTransformType(config);
				} catch (Exception e) {
					LogManager.getLogger().throwing(e);
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
			List<TypeCustom> listType = new ArrayList<>();
			TypeCustom.MAP.forEach((k, v) -> {
				if (v instanceof TypeCustom custom) {
					listType.add(custom);
				}
			});
			buffer.writeInt(listType.size());
			for (TypeCustom type : listType) {
				try {
					Tag tag = CustomFanTypeConfig.CODEC.encodeStart(NbtOps.INSTANCE, type.getConfig())
							.getOrThrow(false, LogManager.getLogger()::error);
					buffer.writeResourceLocation(type.name());
					buffer.writeNbt((CompoundTag) tag);
				} catch (Exception e) {
					LogManager.getLogger().error(e);
				}
			}
			List<CustomTransformConfig> listTransform = new ArrayList<>();
			EntityTransformHelper.LIST.forEach(e -> {
				if (e instanceof CustomTransformType custom) {
					listTransform.add(custom.config);
				}
			});
			buffer.writeInt(listTransform.size());
			for (CustomTransformConfig config : listTransform) {
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
					ResourceLocation id = buffer.readResourceLocation();
					CompoundTag tag = buffer.readAnySizeNbt();
					CustomFanTypeConfig config = CustomFanTypeConfig.CODEC.decode(NbtOps.INSTANCE, tag)
							.getOrThrow(false, LogManager.getLogger()::error).getFirst();
					try {
						new TypeCustom(id, config);
					} catch (Exception e) {
						LogManager.getLogger().throwing(e);
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
						LogManager.getLogger().throwing(e);
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
