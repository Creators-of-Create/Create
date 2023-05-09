package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;

public class TrainPacket extends SimplePacketBase {

	UUID trainId;
	Train train;
	boolean add;

	public TrainPacket(Train train, boolean add) {
		this.train = train;
		this.add = add;
	}

	public TrainPacket(FriendlyByteBuf buffer) {
		add = buffer.readBoolean();
		trainId = buffer.readUUID();

		if (!add)
			return;

		UUID owner = buffer.readUUID();
		List<Carriage> carriages = new ArrayList<>();
		List<Integer> carriageSpacing = new ArrayList<>();

		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			Couple<CarriageBogey> bogies = Couple.create(null, null);
			for (boolean isFirst : Iterate.trueAndFalse) {
				if (!isFirst && !buffer.readBoolean())
					continue;
				AbstractBogeyBlock<?> type = (AbstractBogeyBlock<?>) ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
				boolean upsideDown = buffer.readBoolean();
				CompoundTag data = buffer.readNbt();
				bogies.set(isFirst, new CarriageBogey(type, upsideDown, data, new TravellingPoint(), new TravellingPoint()));
			}
			int spacing = buffer.readVarInt();
			carriages.add(new Carriage(bogies.getFirst(), bogies.getSecond(), spacing));
		}

		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			carriageSpacing.add(buffer.readVarInt());

		boolean doubleEnded = buffer.readBoolean();
		train = new Train(trainId, owner, null, carriages, carriageSpacing, doubleEnded);

		train.name = Component.Serializer.fromJson(buffer.readUtf());
		train.icon = TrainIconType.byId(buffer.readResourceLocation());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(add);
		buffer.writeUUID(train.id);

		if (!add)
			return;

		buffer.writeUUID(train.owner);

		buffer.writeVarInt(train.carriages.size());
		for (Carriage carriage : train.carriages) {
			for (boolean first : Iterate.trueAndFalse) {
				if (!first) {
					boolean onTwoBogeys = carriage.isOnTwoBogeys();
					buffer.writeBoolean(onTwoBogeys);
					if (!onTwoBogeys)
						continue;
				}
				CarriageBogey bogey = carriage.bogeys.get(first);
				buffer.writeResourceLocation(RegisteredObjects.getKeyOrThrow((Block) bogey.type));
				buffer.writeBoolean(bogey.upsideDown);
				buffer.writeNbt(bogey.bogeyData);
			}
			buffer.writeVarInt(carriage.bogeySpacing);
		}

		buffer.writeVarInt(train.carriageSpacing.size());
		train.carriageSpacing.forEach(buffer::writeVarInt);

		buffer.writeBoolean(train.doubleEnded);
		buffer.writeUtf(Component.Serializer.toJson(train.name));
		buffer.writeResourceLocation(train.icon.id);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				Map<UUID, Train> trains = CreateClient.RAILWAYS.trains;
				if (add)
					trains.put(train.id, train);
				else
					trains.remove(trainId);
			});
		context.get()
			.setPacketHandled(true);
	}

}
