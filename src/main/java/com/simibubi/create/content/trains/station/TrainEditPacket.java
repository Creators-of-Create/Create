package com.simibubi.create.content.trains.station;

import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class TrainEditPacket extends SimplePacketBase {

	private String name;
	private UUID id;
	private ResourceLocation iconType;
	private int mapColor;

	public TrainEditPacket(UUID id, String name, ResourceLocation iconType, int mapColor) {
		this.name = name;
		this.id = id;
		this.iconType = iconType;
		this.mapColor = mapColor;
	}

	public TrainEditPacket(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		name = buffer.readUtf(256);
		iconType = buffer.readResourceLocation();
		mapColor = buffer.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeUtf(name);
		buffer.writeResourceLocation(iconType);
		buffer.writeVarInt(mapColor);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			Level level = sender == null ? null : sender.level();
			Train train = Create.RAILWAYS.sided(level).trains.get(id);
			if (train == null)
				return;
			if (!name.isBlank())
				train.name = Components.literal(name);
			train.icon = TrainIconType.byId(iconType);
			train.mapColorIndex = mapColor;
			if (sender != null)
				AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainEditReturnPacket(id, name, iconType, mapColor));
		});
		return true;
	}

	public static class TrainEditReturnPacket extends TrainEditPacket {

		public TrainEditReturnPacket(FriendlyByteBuf buffer) {
			super(buffer);
		}

		public TrainEditReturnPacket(UUID id, String name, ResourceLocation iconType,  int mapColor) {
			super(id, name, iconType, mapColor);
		}

	}

}
