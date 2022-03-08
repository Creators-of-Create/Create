package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SignalEdgeGroupPacket extends SimplePacketBase {

	Collection<UUID> ids;
	boolean add;

	public SignalEdgeGroupPacket(Collection<UUID> ids, boolean add) {
		this.ids = ids;
		this.add = add;
	}

	public SignalEdgeGroupPacket(FriendlyByteBuf buffer) {
		ids = new ArrayList<>();
		add = buffer.readBoolean();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			ids.add(buffer.readUUID());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(add);
		buffer.writeVarInt(ids.size());
		ids.forEach(buffer::writeUUID);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				Map<UUID, SignalEdgeGroup> signalEdgeGroups = CreateClient.RAILWAYS.signalEdgeGroups;
				for (UUID id : ids) {
					if (add)
						signalEdgeGroups.put(id, new SignalEdgeGroup(id));
					else
						signalEdgeGroups.remove(id);
				}
			});
		context.get()
			.setPacketHandled(true);
	}

}
