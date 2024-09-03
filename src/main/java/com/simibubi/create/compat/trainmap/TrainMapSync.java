package com.simibubi.create.compat.trainmap;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.signal.SignalBlock.SignalType;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.network.PacketDistributor;

public class TrainMapSync {

	public static final int lightPacketInterval = 5;
	public static final int fullPacketInterval = 10;

	public static int ticks;

	public enum TrainState {
		RUNNING, RUNNING_MANUALLY, DERAILED, SCHEDULE_INTERRUPTED, CONDUCTOR_MISSING, NAVIGATION_FAILED
	}

	public enum SignalState {
		NOT_WAITING, WAITING_FOR_REDSTONE, BLOCK_SIGNAL, CHAIN_SIGNAL
	}

	public static class TrainMapSyncEntry {

		// Clientside
		public float[] prevPositions;

		// Updated every 5 ticks
		public float[] positions;
		public List<ResourceKey<Level>> dimensions;
		public TrainState state = TrainState.RUNNING;
		public SignalState signalState = SignalState.NOT_WAITING;
		public boolean fueled = false;
		public boolean backwards = false;
		public int targetStationDistance = 0;

		// Updated every 10 ticks
		public String ownerName = "";
		public String targetStationName = "";
		public UUID waitingForTrain = null;

		public void gatherDimensions(DimensionPalette dimensionPalette) {
			for (ResourceKey<Level> resourceKey : dimensions)
				if (resourceKey != null)
					dimensionPalette.encode(resourceKey);
		}

		public void send(FriendlyByteBuf buffer, DimensionPalette dimensionPalette, boolean light) {
			buffer.writeVarInt(positions.length);
			for (float f : positions)
				buffer.writeFloat(f);

			buffer.writeVarInt(dimensions.size());
			for (ResourceKey<Level> resourceKey : dimensions)
				buffer.writeVarInt(resourceKey == null ? -1 : dimensionPalette.encode(resourceKey));

			buffer.writeVarInt(state.ordinal());
			buffer.writeVarInt(signalState.ordinal());
			buffer.writeBoolean(fueled);
			buffer.writeBoolean(backwards);
			buffer.writeVarInt(targetStationDistance);

			if (light)
				return;

			buffer.writeUtf(ownerName);
			buffer.writeUtf(targetStationName);

			buffer.writeBoolean(waitingForTrain != null);
			if (waitingForTrain != null)
				buffer.writeUUID(waitingForTrain);
		}

		public void receive(FriendlyByteBuf buffer, DimensionPalette dimensionPalette, boolean light) {
			positions = new float[buffer.readVarInt()];
			for (int i = 0; i < positions.length; i++)
				positions[i] = buffer.readFloat();

			dimensions = new ArrayList<>();
			int dimensionsSize = buffer.readVarInt();
			for (int i = 0; i < dimensionsSize; i++) {
				int index = buffer.readVarInt();
				dimensions.add(index == -1 ? null : dimensionPalette.decode(index));
			}

			state = TrainState.values()[buffer.readVarInt()];
			signalState = SignalState.values()[buffer.readVarInt()];
			fueled = buffer.readBoolean();
			backwards = buffer.readBoolean();
			targetStationDistance = buffer.readVarInt();

			if (light)
				return;

			ownerName = buffer.readUtf();
			targetStationName = buffer.readUtf();

			waitingForTrain = null;
			if (buffer.readBoolean())
				waitingForTrain = buffer.readUUID();
		}

		public void updateFrom(TrainMapSyncEntry other, boolean light) {
			prevPositions = positions;

			positions = other.positions;
			state = other.state;
			signalState = other.signalState;
			fueled = other.fueled;
			backwards = other.backwards;
			targetStationDistance = other.targetStationDistance;

			if (light)
				return;

			ownerName = other.ownerName;
			targetStationName = other.targetStationName;
			waitingForTrain = other.waitingForTrain;
		}

		public Vec3 getPosition(int carriageIndex, boolean firstBogey, double time) {
			int startIndex = carriageIndex * 6 + (firstBogey ? 0 : 3);
			if (positions == null || positions.length <= startIndex + 2)
				return Vec3.ZERO;
			Vec3 position = new Vec3(positions[startIndex], positions[startIndex + 1], positions[startIndex + 2]);
			if (prevPositions == null || prevPositions.length <= startIndex + 2)
				return position;
			Vec3 prevPosition =
				new Vec3(prevPositions[startIndex], prevPositions[startIndex + 1], prevPositions[startIndex + 2]);
			return prevPosition.lerp(position, time);
		}

	}

	public static Cache<UUID, WeakReference<ServerPlayer>> requestingPlayers = CacheBuilder.newBuilder()
		.expireAfterWrite(Duration.ofSeconds(1))
		.build();

	public static void requestReceived(ServerPlayer sender) {
		boolean sendImmediately = requestingPlayers.getIfPresent(sender.getUUID()) == null;
		requestingPlayers.put(sender.getUUID(), new WeakReference<>(sender));
		if (sendImmediately)
			send(sender.server, false);
	}

	public static void serverTick(ServerTickEvent event) {
		ticks++;
		if (ticks % fullPacketInterval == 0)
			send(event.getServer(), false);
		else if (ticks % lightPacketInterval == 0)
			send(event.getServer(), true);
	}

	public static void send(MinecraftServer minecraftServer, boolean light) {
		if (requestingPlayers.size() == 0)
			return;

		TrainMapSyncPacket packet = new TrainMapSyncPacket(light);
		for (Train train : Create.RAILWAYS.trains.values())
			packet.add(train.id, createEntry(minecraftServer, train));

		for (WeakReference<ServerPlayer> weakReference : requestingPlayers.asMap()
			.values()) {
			ServerPlayer player = weakReference.get();
			if (player == null)
				continue;
			AllPackets.getChannel()
				.send(PacketDistributor.PLAYER.with(() -> player), packet);
		}
	}

	private static TrainMapSyncEntry createEntry(MinecraftServer minecraftServer, Train train) {
		TrainMapSyncEntry entry = new TrainMapSyncEntry();
		boolean stopped = Math.abs(train.speed) < 0.05;

		entry.positions = new float[train.carriages.size() * 6];
		entry.dimensions = new ArrayList<>();

		List<Carriage> carriages = train.carriages;
		for (int i = 0; i < carriages.size(); i++) {
			Carriage carriage = carriages.get(i);
			Vec3 leadingPos;
			Vec3 trailingPos;

			if (train.graph == null) {

				// Train is derailed
				Pair<ResourceKey<Level>, DimensionalCarriageEntity> dimCarriage =
					carriage.anyAvailableDimensionalCarriage();
				if (dimCarriage == null || carriage.presentInMultipleDimensions()) {
					entry.dimensions.add(null);
					continue;
				}

				leadingPos = dimCarriage.getSecond().rotationAnchors.getFirst();
				trailingPos = dimCarriage.getSecond().rotationAnchors.getSecond();

				if (leadingPos == null || trailingPos == null) {
					entry.dimensions.add(null);
					continue;
				}

				entry.dimensions.add(dimCarriage.getFirst());

			} else {

				// Train is on Track
				TravellingPoint leading = carriage.getLeadingPoint();
				TravellingPoint trailing = carriage.getTrailingPoint();
				if (leading == null || trailing == null || leading.edge == null || trailing.edge == null) {
					entry.dimensions.add(null);
					continue;
				}

				ResourceKey<Level> leadingDim =
					(leading.node1 == null || leading.edge == null || leading.edge.isInterDimensional()) ? null
						: leading.node1.getLocation()
							.getDimension();

				ResourceKey<Level> trailingDim =
					(trailing.node1 == null || trailing.edge == null || trailing.edge.isInterDimensional()) ? null
						: trailing.node1.getLocation()
							.getDimension();

				ResourceKey<Level> carriageDim = (leadingDim == null || leadingDim != trailingDim) ? null : leadingDim;
				entry.dimensions.add(carriageDim);
				
				leadingPos = leading.getPosition(train.graph);
				trailingPos = trailing.getPosition(train.graph);
			}

			entry.positions[i * 6] = (float) leadingPos.x();
			entry.positions[i * 6 + 1] = (float) leadingPos.y();
			entry.positions[i * 6 + 2] = (float) leadingPos.z();

			entry.positions[i * 6 + 3] = (float) trailingPos.x();
			entry.positions[i * 6 + 4] = (float) trailingPos.y();
			entry.positions[i * 6 + 5] = (float) trailingPos.z();
		}

		entry.backwards = train.currentlyBackwards;

		if (train.owner != null) {
			ServerPlayer owner = minecraftServer.getPlayerList()
				.getPlayer(train.owner);
			if (owner != null)
				entry.ownerName = owner.getName()
					.getString();
		}

		if (train.derailed) {
			entry.state = TrainState.DERAILED;
			return entry;
		}

		ScheduleRuntime runtime = train.runtime;
		if (runtime.getSchedule() != null && stopped) {
			if (runtime.paused) {
				entry.state = TrainState.SCHEDULE_INTERRUPTED;
				return entry;
			}

			if (train.status.conductor) {
				entry.state = TrainState.CONDUCTOR_MISSING;
				return entry;
			}

			if (train.status.navigation) {
				entry.state = TrainState.NAVIGATION_FAILED;
				return entry;
			}
		}

		if ((runtime.getSchedule() == null || runtime.paused) && train.speed != 0)
			entry.state = TrainState.RUNNING_MANUALLY;

		GlobalStation currentStation = train.getCurrentStation();
		if (currentStation != null) {
			entry.targetStationName = currentStation.name;
			entry.targetStationDistance = 0;
		} else if (train.navigation.destination != null && !runtime.paused) {
			entry.targetStationName = train.navigation.destination.name;
			entry.targetStationDistance = Math.max(0, Mth.floor(train.navigation.distanceToDestination));
		}

		if (stopped && train.navigation.waitingForSignal != null) {
			UUID signalId = train.navigation.waitingForSignal.getFirst();
			boolean side = train.navigation.waitingForSignal.getSecond();
			SignalBoundary signal = train.graph.getPoint(EdgePointType.SIGNAL, signalId);

			if (signal != null) {
				boolean chainSignal = signal.types.get(side) == SignalType.CROSS_SIGNAL;
				entry.signalState = chainSignal ? SignalState.CHAIN_SIGNAL : SignalState.BLOCK_SIGNAL;
				if (signal.isForcedRed(side))
					entry.signalState = SignalState.WAITING_FOR_REDSTONE;
				else {
					SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(signal.groups.get(side));
					if (group != null) {
						for (Train other : group.trains) {
							if (other == train)
								continue;
							entry.waitingForTrain = other.id;
							break;
						}
					}
				}
			}
		}

		if (train.fuelTicks > 0 && !stopped)
			entry.fueled = true;

		return entry;
	}

}
