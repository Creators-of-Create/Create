package com.simibubi.create.compat.trainmap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.compat.trainmap.TrainMapSync.TrainMapSyncEntry;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrainMapSyncPacket extends SimplePacketBase {

	public List<Pair<UUID, TrainMapSyncEntry>> entries = new ArrayList<>();
	public boolean light;

	public TrainMapSyncPacket(boolean light) {
		this.light = light;
	}

	public void add(UUID trainId, TrainMapSyncEntry data) {
		entries.add(Pair.of(trainId, data));
	}

	public TrainMapSyncPacket(FriendlyByteBuf buffer) {
		DimensionPalette dimensionPalette = DimensionPalette.receive(buffer);
		light = buffer.readBoolean();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			UUID id = buffer.readUUID();
			TrainMapSyncEntry entry = new TrainMapSyncEntry();
			entry.receive(buffer, dimensionPalette, light);
			entries.add(Pair.of(id, entry));
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		DimensionPalette dimensionPalette = new DimensionPalette();
		for (Pair<UUID, TrainMapSyncEntry> pair : entries)
			pair.getSecond()
				.gatherDimensions(dimensionPalette);

		dimensionPalette.send(buffer);
		buffer.writeBoolean(light);

		buffer.writeVarInt(entries.size());
		for (Pair<UUID, TrainMapSyncEntry> pair : entries) {
			buffer.writeUUID(pair.getFirst());
			pair.getSecond()
				.send(buffer, dimensionPalette, light);
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> TrainMapSyncClient.receive(this));
		return true;
	}

}
