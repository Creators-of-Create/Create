package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TrainIconType;
import com.simibubi.create.foundation.networking.AllPackets;
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

	public TrainEditPacket(UUID id, String name, ResourceLocation iconType) {
		this.name = name;
		this.id = id;
		this.iconType = iconType;
	}

	public TrainEditPacket(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		name = buffer.readUtf(256);
		iconType = buffer.readResourceLocation();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeUtf(name);
		buffer.writeResourceLocation(iconType);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.getSender();
			Level level = sender == null ? null : sender.level;
			Train train = Create.RAILWAYS.sided(level).trains.get(id);
			if (train == null)
				return;
			if (!name.isBlank())
				train.name = Components.literal(name);
			train.icon = TrainIconType.byId(iconType);
			if (sender != null)
				AllPackets.channel.send(PacketDistributor.ALL.noArg(), new TrainEditReturnPacket(id, name, iconType));
		});
		ctx.setPacketHandled(true);
	}

	public static class TrainEditReturnPacket extends TrainEditPacket {

		public TrainEditReturnPacket(FriendlyByteBuf buffer) {
			super(buffer);
		}

		public TrainEditReturnPacket(UUID id, String name, ResourceLocation iconType) {
			super(id, name, iconType);
		}

	}

}
