package com.simibubi.create.content.trains.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SignalEdgeGroupPacket extends SimplePacketBase {

	List<UUID> ids;
	List<EdgeGroupColor> colors;
	boolean add;

	public SignalEdgeGroupPacket(UUID id, EdgeGroupColor color) {
		this(ImmutableList.of(id), ImmutableList.of(color), true);
	}

	public SignalEdgeGroupPacket(List<UUID> ids, List<EdgeGroupColor> colors, boolean add) {
		this.ids = ids;
		this.colors = colors;
		this.add = add;
	}

	public SignalEdgeGroupPacket(FriendlyByteBuf buffer) {
		ids = new ArrayList<>();
		colors = new ArrayList<>();
		add = buffer.readBoolean();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			ids.add(buffer.readUUID());
		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			colors.add(EdgeGroupColor.values()[buffer.readVarInt()]);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(add);
		buffer.writeVarInt(ids.size());
		ids.forEach(buffer::writeUUID);
		buffer.writeVarInt(colors.size());
		colors.forEach(c -> buffer.writeVarInt(c.ordinal()));
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Map<UUID, SignalEdgeGroup> signalEdgeGroups = CreateClient.RAILWAYS.signalEdgeGroups;
			int i = 0;
			for (UUID id : ids) {
				if (!add) {
					signalEdgeGroups.remove(id);
					continue;
				}

				SignalEdgeGroup group = new SignalEdgeGroup(id);
				signalEdgeGroups.put(id, group);
				if (colors.size() > i)
					group.color = colors.get(i);
				i++;
			}
		});
		return true;
	}

}
