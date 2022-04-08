package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.IEdgePointListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgeData;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBlock.SignalType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBoundary;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleRuntime;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleRuntime.State;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class Train {

	public double speed = 0;
	public double targetSpeed = 0;

	public UUID id;
	public UUID owner;
	public TrackGraph graph;
	public Navigation navigation;
	public ScheduleRuntime runtime;
	public TrainIconType icon;
	public Component name;
	public TrainStatus status;

	public SteerDirection manualSteer;
	public boolean manualTick;

	public UUID currentStation;
	public boolean currentlyBackwards;

	public boolean heldForAssembly;
	public boolean doubleEnded;
	public List<Carriage> carriages;
	public List<Integer> carriageSpacing;

	public boolean updateSignalBlocks;
	public Map<UUID, UUID> occupiedSignalBlocks;
	public Set<UUID> reservedSignalBlocks;

	List<TrainMigration> migratingPoints;
	public int migrationCooldown;
	public boolean derailed;

	int tickOffset;
	double[] stress;

	public Train(UUID id, UUID owner, TrackGraph graph, List<Carriage> carriages, List<Integer> carriageSpacing,
		boolean doubleEnded) {

		this.id = id;
		this.owner = owner;
		this.graph = graph;
		this.carriages = carriages;
		this.carriageSpacing = carriageSpacing;
		this.icon = TrainIconType.getDefault();
		this.stress = new double[carriageSpacing.size()];
		this.name = Lang.translate("train.unnamed");
		this.status = new TrainStatus(this);
		this.doubleEnded = doubleEnded;

		carriages.forEach(c -> c.setTrain(this));

		navigation = new Navigation(this);
		runtime = new ScheduleRuntime(this);
		heldForAssembly = true;
		migratingPoints = new ArrayList<>();
		currentStation = null;
		manualSteer = SteerDirection.NONE;
		occupiedSignalBlocks = new HashMap<>();
		reservedSignalBlocks = new HashSet<>();
		tickOffset = Create.RANDOM.nextInt(100);
	}

	public void earlyTick(Level level) {
		status.tick(level);
		if (graph == null && !migratingPoints.isEmpty())
			reattachToTracks(level);
		if (graph == null) {
			addToSignalGroups(occupiedSignalBlocks.keySet());
			return;
		}
		
		if (updateSignalBlocks) {
			updateSignalBlocks = false;
			collectInitiallyOccupiedSignalBlocks();
		}

		addToSignalGroups(occupiedSignalBlocks.keySet());
		addToSignalGroups(reservedSignalBlocks);
	}

	private void addToSignalGroups(Collection<UUID> groups) {
		Map<UUID, SignalEdgeGroup> groupMap = Create.RAILWAYS.signalEdgeGroups;
		for (Iterator<UUID> iterator = groups.iterator(); iterator.hasNext();) {
			SignalEdgeGroup signalEdgeGroup = groupMap.get(iterator.next());
			if (signalEdgeGroup == null)
				iterator.remove();
			else
				signalEdgeGroup.trains.add(this);
		}
	}

	public void tick(Level level) {
		Create.RAILWAYS.markTracksDirty();

		if (graph == null) {
			carriages.forEach(c -> c.manageEntity(level));
			updateConductors();
			return;
		}

		updateConductors();
		runtime.tick(level);
		navigation.tick(level);

		tickPassiveSlowdown();
		if (derailed)
			tickDerailedSlowdown();

		double distance = speed;
		Carriage previousCarriage = null;
		int carriageCount = carriages.size();

		for (int i = 0; i < carriageCount; i++) {
			Carriage carriage = carriages.get(i);
			if (previousCarriage != null) {
				int target = carriageSpacing.get(i - 1);
				Vec3 leadingAnchor = carriage.leadingAnchor();
				Vec3 trailingAnchor = previousCarriage.trailingAnchor();
				double actual = leadingAnchor.distanceTo(trailingAnchor);
				stress[i - 1] = target - actual;
			}
			previousCarriage = carriage;
		}

		// positive stress: carriages should move apart
		// negative stress: carriages should move closer

		boolean approachingStation = navigation.distanceToDestination < 5;
		double leadingModifier = approachingStation ? 0.75d : 0.5d;
		double trailingModifier = approachingStation ? 0d : 0.125d;

		boolean blocked = false;
		boolean iterateFromBack = speed < 0;

		for (int index = 0; index < carriageCount; index++) {
			int i = iterateFromBack ? carriageCount - 1 - index : index;
			double leadingStress = i == 0 ? 0 : stress[i - 1] * -(iterateFromBack ? trailingModifier : leadingModifier);
			double trailingStress =
				i == stress.length ? 0 : stress[i] * (iterateFromBack ? leadingModifier : trailingModifier);

			Carriage carriage = carriages.get(i);

			TravellingPoint toFollowForward = i == 0 ? null
				: carriages.get(i - 1)
					.getTrailingPoint();

			TravellingPoint toFollowBackward = i == carriageCount - 1 ? null
				: carriages.get(i + 1)
					.getLeadingPoint();

			Function<TravellingPoint, ITrackSelector> forwardControl =
				toFollowForward == null ? navigation::control : mp -> mp.follow(toFollowForward);
			Function<TravellingPoint, ITrackSelector> backwardControl =
				toFollowBackward == null ? navigation::control : mp -> mp.follow(toFollowBackward);

			double totalStress = leadingStress + trailingStress;
			boolean first = i == 0;
			boolean last = i == carriageCount - 1;
			int carriageType = first ? last ? Carriage.BOTH : Carriage.FIRST : last ? Carriage.LAST : Carriage.MIDDLE;
			double actualDistance =
				carriage.travel(level, graph, distance + totalStress, forwardControl, backwardControl, carriageType);
			blocked |= carriage.blocked;

			if (index == 0) {
				distance = actualDistance;
				collideWithOtherTrains(level, carriage);
				if (graph == null)
					return;
			}
		}

		if (blocked) {
			speed = 0;
			navigation.cancelNavigation();
			runtime.tick(level);
			status.endOfTrack();
		} else if (speed != 0)
			status.trackOK();

		updateNavigationTarget(distance);
	}

	public IEdgePointListener frontSignalListener() {
		return (distance, couple) -> {

			if (couple.getFirst()instanceof GlobalStation station) {
				if (!station.canApproachFrom(couple.getSecond()
					.getSecond()) || navigation.destination != station)
					return false;
				speed = 0;
				navigation.distanceToDestination = 0;
				navigation.currentPath.clear();
				arriveAt(navigation.destination);
				navigation.destination = null;
				return true;
			}

			if (!(couple.getFirst()instanceof SignalBoundary signal))
				return false;
			if (navigation.waitingForSignal != null && navigation.waitingForSignal.getFirst()
				.equals(signal.id)) {
				speed = 0;
				navigation.distanceToSignal = 0;
				return true;
			}

			UUID groupId = signal.getGroup(couple.getSecond()
				.getSecond());
			SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
			if (signalEdgeGroup == null)
				return false;
			signalEdgeGroup.reserved = signal;
			occupy(groupId, signal.id);
			return false;

		};
	}

	private boolean occupy(UUID groupId, @Nullable UUID boundaryId) {
		reservedSignalBlocks.remove(groupId);
		if (boundaryId != null && occupiedSignalBlocks.containsKey(groupId))
			if (boundaryId.equals(occupiedSignalBlocks.get(groupId)))
				return false;
		return occupiedSignalBlocks.put(groupId, boundaryId) == null;
	}

	public IEdgePointListener backSignalListener() {
		return (distance, couple) -> {
			if (!(couple.getFirst()instanceof SignalBoundary signal))
				return false;
			UUID groupId = signal.getGroup(couple.getSecond()
				.getFirst());
			occupiedSignalBlocks.remove(groupId);
			return false;
		};
	}

	private void updateNavigationTarget(double distance) {
		if (navigation.destination == null)
			return;

		Pair<UUID, Boolean> blockingSignal = navigation.waitingForSignal;
		boolean fullRefresh = navigation.distanceToDestination > 100 && navigation.distanceToDestination % 100 > 20;
		boolean signalRefresh = blockingSignal != null && navigation.distanceToSignal % 50 > 5;
		boolean partialRefresh = navigation.distanceToDestination < 100 && navigation.distanceToDestination % 50 > 5;

		double toSubstract = navigation.destinationBehindTrain ? -distance : distance;
		boolean navigatingManually = runtime.paused;

		navigation.distanceToDestination -= toSubstract;
		if (blockingSignal != null) {
			navigation.distanceToSignal -= toSubstract;
			signalRefresh &= navigation.distanceToSignal % 50 < 5;
		}

		fullRefresh &= navigation.distanceToDestination % 100 <= 20;
		partialRefresh &= navigation.distanceToDestination % 50 <= 5;

		if (blockingSignal != null && navigation.ticksWaitingForSignal % 100 == 50) {
			SignalBoundary signal = graph.getPoint(EdgePointType.SIGNAL, blockingSignal.getFirst());
			fullRefresh |= signal != null && signal.types.get(blockingSignal.getSecond()) == SignalType.CROSS_SIGNAL;
		}

		if (signalRefresh)
			navigation.waitingForSignal = null;
		if (!fullRefresh && !partialRefresh)
			return;
		if (!reservedSignalBlocks.isEmpty())
			return;

		GlobalStation destination = navigation.destination;
		if (!navigatingManually && fullRefresh) {
			GlobalStation preferredDestination = runtime.findNextStation();
			if (preferredDestination != null)
				destination = preferredDestination;
		}

		navigation.startNavigation(destination, navigatingManually ? -1 : Double.MAX_VALUE, false);
	}

	private void tickDerailedSlowdown() {
		speed /= 3f;
		if (Mth.equal(speed, 0))
			speed = 0;
	}

	private void tickPassiveSlowdown() {
		if (!manualTick && navigation.destination == null && speed != 0) {
			double acceleration = AllConfigs.SERVER.trains.getAccelerationMPTT();
			if (speed > 0) {
				speed = Math.max(speed - acceleration, 0);
			} else
				speed = Math.min(speed + acceleration, 0);
		}
		manualTick = false;
	}

	private void updateConductors() {
		for (Carriage carriage : carriages)
			carriage.updateConductors();
	}

	public boolean hasForwardConductor() {
		for (Carriage carriage : carriages)
			if (carriage.presentConductors.getFirst())
				return true;
		return false;
	}

	public boolean hasBackwardConductor() {
		for (Carriage carriage : carriages)
			if (carriage.presentConductors.getSecond())
				return true;
		return false;
	}

	private void collideWithOtherTrains(Level level, Carriage carriage) {
		if (derailed)
			return;

		Vec3 start = (speed < 0 ? carriage.getTrailingPoint() : carriage.getLeadingPoint()).getPosition();
		Vec3 end = (speed < 0 ? carriage.getLeadingPoint() : carriage.getTrailingPoint()).getPosition();

		Pair<Train, Vec3> collision = findCollidingTrain(level, start, end, this);
		if (collision == null)
			return;

		Train train = collision.getFirst();

		double combinedSpeed = Math.abs(speed) + Math.abs(train.speed);
		if (combinedSpeed > .2f) {
			Vec3 v = collision.getSecond();
			level.explode(null, v.x, v.y, v.z, (float) Math.min(3 * combinedSpeed, 5), BlockInteraction.NONE);
		}

		crash();
		train.crash();
	}

	public static Pair<Train, Vec3> findCollidingTrain(Level level, Vec3 start, Vec3 end, Train ignore) {
		for (Train train : Create.RAILWAYS.sided(level).trains.values()) {
			if (train == ignore)
				continue;

			Vec3 diff = end.subtract(start);
			Vec3 lastPoint = null;

			for (Carriage otherCarriage : train.carriages) {
				for (boolean betweenBits : Iterate.trueAndFalse) {
					if (betweenBits && lastPoint == null)
						continue;

					TravellingPoint otherLeading = otherCarriage.getLeadingPoint();
					TravellingPoint otherTrailing = otherCarriage.getTrailingPoint();
					if (otherLeading.edge == null || otherTrailing.edge == null)
						continue;

					Vec3 start2 = otherLeading.getPosition();
					Vec3 end2 = otherTrailing.getPosition();
					if (betweenBits) {
						end2 = start2;
						start2 = lastPoint;
					}

					lastPoint = end2;

					if ((end.y < end2.y - 3 || end2.y < end.y - 3)
						&& (start.y < start2.y - 3 || start2.y < start.y - 3))
						continue;

					Vec3 diff2 = end2.subtract(start2);
					Vec3 normedDiff = diff.normalize();
					Vec3 normedDiff2 = diff2.normalize();
					double[] intersect = VecHelper.intersect(start, start2, normedDiff, normedDiff2, Axis.Y);

					if (intersect == null) {
						Vec3 intersectSphere = VecHelper.intersectSphere(start2, normedDiff2, start, .125f);
						if (intersectSphere == null)
							continue;
						if (!Mth.equal(normedDiff2.dot(intersectSphere.subtract(start2)
							.normalize()), 1))
							continue;
						intersect = new double[2];
						intersect[0] = intersectSphere.distanceTo(start) - .125;
						intersect[1] = intersectSphere.distanceTo(start2) - .125;
					}

					if (intersect[0] > diff.length())
						continue;
					if (intersect[1] > diff2.length())
						continue;
					if (intersect[0] < 0)
						continue;
					if (intersect[1] < 0)
						continue;

					return Pair.of(train, start.add(normedDiff.scale(intersect[0])));
				}
			}
		}
		return null;
	}

	public void crash() {
		navigation.cancelNavigation();
		if (derailed)
			return;
		speed = -Mth.clamp(speed, -.5, .5);
		derailed = true;
		graph = null;
		syncTrackGraphChanges();
		status.crash();
	}

	public boolean disassemble(Direction assemblyDirection, BlockPos pos) {
		if (!canDisassemble())
			return false;

		int offset = 1;
		boolean backwards = currentlyBackwards;
		for (int i = 0; i < carriages.size(); i++) {

			Carriage carriage = carriages.get(backwards ? carriages.size() - i - 1 : i);
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity == null)
				return false;

			entity.setPos(Vec3
				.atLowerCornerOf(pos.relative(assemblyDirection, backwards ? offset + carriage.bogeySpacing : offset)));
			entity.disassemble();

			offset += carriage.bogeySpacing;

			if (i < carriageSpacing.size())
				offset += carriageSpacing.get(backwards ? carriageSpacing.size() - i - 1 : i);
		}

		GlobalStation currentStation = getCurrentStation();
		if (currentStation != null)
			currentStation.cancelReservation(this);

		Create.RAILWAYS.removeTrain(id);
		AllPackets.channel.send(PacketDistributor.ALL.noArg(), new TrainPacket(this, false));
		return true;
	}

	public boolean canDisassemble() {
		for (Carriage carriage : carriages) {
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity == null)
				return false;
			if (!Mth.equal(entity.pitch, 0))
				return false;
			if (!Mth.equal(((entity.yaw % 90) + 360) % 90, 0))
				return false;
		}
		return true;
	}

	public boolean isTravellingOn(TrackNode node) {
		MutableBoolean affected = new MutableBoolean(false);
		forEachTravellingPoint(tp -> {
			if (tp.node1 == node || tp.node2 == node)
				affected.setTrue();
		});
		return affected.booleanValue();
	}

	public void detachFromTracks() {
		migratingPoints.clear();
		navigation.cancelNavigation();
		forEachTravellingPoint(tp -> migratingPoints.add(new TrainMigration(tp)));
		graph = null;
		syncTrackGraphChanges();
	}

	public void forEachTravellingPoint(Consumer<TravellingPoint> callback) {
		for (Carriage c : carriages) {
			c.leadingBogey().points.forEach(callback::accept);
			if (c.isOnTwoBogeys())
				c.trailingBogey().points.forEach(callback::accept);
		}
	}

	public void forEachTravellingPointBackwards(BiConsumer<TravellingPoint, Double> callback) {
		double lastWheelOffset = 0;
		for (int i = 0; i < carriages.size(); i++) {
			int index = carriages.size() - i - 1;
			Carriage carriage = carriages.get(index);
			CarriageBogey trailingBogey = carriage.trailingBogey();
			double trailSpacing = trailingBogey.type.getWheelPointSpacing();

			// trailing point
			callback.accept(trailingBogey.trailing(),
				i == 0 ? 0 : carriageSpacing.get(index) - lastWheelOffset - trailSpacing / 2);

			// inside 1st bogey
			callback.accept(trailingBogey.leading(), trailSpacing);

			lastWheelOffset = trailSpacing / 2;

			if (!carriage.isOnTwoBogeys())
				continue;

			CarriageBogey leadingBogey = carriage.leadingBogey();
			double leadSpacing = carriage.leadingBogey().type.getWheelPointSpacing();

			// between bogeys
			callback.accept(leadingBogey.trailing(), carriage.bogeySpacing - lastWheelOffset - leadSpacing / 2);

			// inside 2nd bogey
			callback.accept(trailingBogey.leading(), leadSpacing);

			lastWheelOffset = leadSpacing / 2;
		}
	}

	public void reattachToTracks(Level level) {
		if (migrationCooldown > 0) {
			migrationCooldown--;
			return;
		}

		Set<Entry<UUID, TrackGraph>> entrySet = new HashSet<>(Create.RAILWAYS.trackNetworks.entrySet());
		Map<UUID, List<GraphLocation>> successfulMigrations = new HashMap<>();
		for (TrainMigration md : migratingPoints) {
			for (Iterator<Entry<UUID, TrackGraph>> iterator = entrySet.iterator(); iterator.hasNext();) {
				Entry<UUID, TrackGraph> entry = iterator.next();
				GraphLocation gl = md.tryMigratingTo(entry.getValue());
				if (gl == null) {
					iterator.remove();
					continue;
				}
				successfulMigrations.computeIfAbsent(entry.getKey(), uuid -> new ArrayList<>())
					.add(gl);
			}
		}

		if (entrySet.isEmpty()) {
			migrationCooldown = 40;
			status.failedMigration();
			derailed = true;
			syncTrackGraphChanges();
			return;
		}

		for (Entry<UUID, TrackGraph> entry : entrySet) {
			graph = entry.getValue();
			List<GraphLocation> locations = successfulMigrations.get(entry.getKey());
			forEachTravellingPoint(tp -> tp.migrateTo(locations));
			migratingPoints.clear();
			if (derailed)
				status.successfulMigration();
			derailed = false;
			if (runtime.getSchedule() != null) {
				if (runtime.state == State.IN_TRANSIT)
					runtime.state = State.PRE_TRANSIT;
			}
			GlobalStation currentStation = getCurrentStation();
			if (currentStation != null)
				currentStation.reserveFor(this);
			updateSignalBlocks = true;
			syncTrackGraphChanges();
			return;
		}
	}

	public void syncTrackGraphChanges() {
		for (Carriage carriage : carriages) {
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity != null)
				entity.setGraph(graph == null ? null : graph.id);
		}
	}

	public int getTotalLength() {
		int length = 0;
		for (int i = 0; i < carriages.size(); i++) {
			Carriage carriage = carriages.get(i);
			if (i == 0)
				length += carriage.leadingBogey().type.getWheelPointSpacing() / 2;
			if (i == carriages.size() - 1)
				length += carriage.trailingBogey().type.getWheelPointSpacing() / 2;

			length += carriage.bogeySpacing;
			if (i < carriageSpacing.size())
				length += carriageSpacing.get(i);
		}
		return length;
	}

	public void leaveStation() {
		GlobalStation currentStation = getCurrentStation();
		if (currentStation != null)
			currentStation.trainDeparted(this);
		this.currentStation = null;
	}

	public void arriveAt(GlobalStation station) {
		setCurrentStation(station);
		reservedSignalBlocks.clear();
		runtime.destinationReached();
	}

	public void setCurrentStation(GlobalStation station) {
		currentStation = station.id;
	}

	public GlobalStation getCurrentStation() {
		if (currentStation == null)
			return null;
		if (graph == null)
			return null;
		return graph.getPoint(EdgePointType.STATION, currentStation);
	}

	@Nullable
	public LivingEntity getOwner(Level level) {
		try {
			UUID uuid = owner;
			return uuid == null ? null : level.getPlayerByUUID(uuid);
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void approachTargetSpeed(float accelerationMod) {
		if (Mth.equal(targetSpeed, speed))
			return;
		if (manualTick)
			leaveStation();
		double acceleration = AllConfigs.SERVER.trains.getAccelerationMPTT();
		if (speed < targetSpeed)
			speed = Math.min(speed + acceleration * accelerationMod, targetSpeed);
		else if (speed > targetSpeed)
			speed = Math.max(speed - acceleration * accelerationMod, targetSpeed);
	}

	public void collectInitiallyOccupiedSignalBlocks() {
		TravellingPoint trailingPoint = carriages.get(carriages.size() - 1)
			.getTrailingPoint();
		TrackNode node1 = trailingPoint.node1;
		TrackNode node2 = trailingPoint.node2;
		TrackEdge edge = trailingPoint.edge;
		double position = trailingPoint.position;
		EdgeData signalData = edge.getEdgeData();

		occupiedSignalBlocks.clear();
		reservedSignalBlocks.clear();

		TravellingPoint signalScout = new TravellingPoint(node1, node2, edge, position);
		Map<UUID, SignalEdgeGroup> allGroups = Create.RAILWAYS.signalEdgeGroups;
		MutableObject<UUID> prevGroup = new MutableObject<>(null);

		if (signalData.hasSignalBoundaries()) {
			SignalBoundary nextBoundary = signalData.next(EdgePointType.SIGNAL, node1, node2, edge, position);
			if (nextBoundary == null) {
				double d = 0;
				SignalBoundary prev = null;
				SignalBoundary current = signalData.next(EdgePointType.SIGNAL, node1, node2, edge, 0);
				while (current != null) {
					prev = current;
					d = current.getLocationOn(node1, node2, edge);
					current = signalData.next(EdgePointType.SIGNAL, node1, node2, edge, d);
				}
				if (prev != null) {
					UUID group = prev.getGroup(node2);
					if (Create.RAILWAYS.signalEdgeGroups.containsKey(group)) {
						occupy(group, null);
						prevGroup.setValue(group);
					}
				}

			} else {
				UUID group = nextBoundary.getGroup(node1);
				if (Create.RAILWAYS.signalEdgeGroups.containsKey(group)) {
					occupy(group, null);
					prevGroup.setValue(group);
				}
			}

		} else if (signalData.singleSignalGroup != null && allGroups.containsKey(signalData.singleSignalGroup)) {
			occupy(signalData.singleSignalGroup, null);
			prevGroup.setValue(signalData.singleSignalGroup);
		}

		forEachTravellingPointBackwards((tp, d) -> {
			signalScout.travel(graph, d, signalScout.follow(tp), (distance, couple) -> {
				if (!(couple.getFirst()instanceof SignalBoundary signal))
					return false;
				couple.getSecond()
					.map(signal::getGroup)
					.forEach(id -> {
						if (!Create.RAILWAYS.signalEdgeGroups.containsKey(id))
							return;
						if (id.equals(prevGroup.getValue()))
							return;
						occupy(id, null);
						prevGroup.setValue(id);
					});
				return false;
			}, signalScout.ignoreTurns());
		});

	}
	
	public boolean shouldCarriageSyncThisTick(long gameTicks, int updateInterval) {
		return (gameTicks + tickOffset) % updateInterval == 0; 
	}

	public Couple<Couple<TrackNode>> getEndpointEdges() {
		return Couple.create(carriages.get(0)
			.getLeadingPoint(),
			carriages.get(carriages.size() - 1)
				.getTrailingPoint())
			.map(tp -> Couple.create(tp.node1, tp.node2));
	}

	public static class Penalties {
		static final int STATION = 200, STATION_WITH_TRAIN = 300;
		static final int MANUAL_TRAIN = 200, IDLE_TRAIN = 700, ARRIVING_TRAIN = 50, WAITING_TRAIN = 50, ANY_TRAIN = 25;
	}

	public int getNavigationPenalty() {
		if (manualTick)
			return Penalties.MANUAL_TRAIN;
		if (runtime.getSchedule() == null || runtime.paused)
			return Penalties.IDLE_TRAIN;
		if (navigation.waitingForSignal != null && navigation.ticksWaitingForSignal > 0)
			return Penalties.WAITING_TRAIN + Math.min(navigation.ticksWaitingForSignal / 20, 1000);
		if (navigation.destination != null && navigation.distanceToDestination < 50 || navigation.distanceToSignal < 20)
			return Penalties.ARRIVING_TRAIN;
		return Penalties.ANY_TRAIN;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.putUUID("Owner", owner);
		if (graph != null)
			tag.putUUID("Graph", graph.id);
		tag.put("Carriages", NBTHelper.writeCompoundList(carriages, Carriage::write));
		tag.putIntArray("CarriageSpacing", carriageSpacing);
		tag.putBoolean("DoubleEnded", doubleEnded);
		tag.putDouble("Speed", speed);
		tag.putDouble("TargetSpeed", targetSpeed);
		tag.putString("IconType", icon.id.toString());
		tag.putString("Name", Component.Serializer.toJson(name));
		if (currentStation != null)
			tag.putUUID("Station", currentStation);
		tag.putBoolean("Backwards", currentlyBackwards);
		tag.putBoolean("StillAssembling", heldForAssembly);
		tag.putBoolean("Derailed", derailed);
		tag.putBoolean("UpdateSignals", updateSignalBlocks);
		tag.put("SignalBlocks", NBTHelper.writeCompoundList(occupiedSignalBlocks.entrySet(), e -> {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putUUID("Id", e.getKey());
			if (e.getValue() != null)
				compoundTag.putUUID("Boundary", e.getValue());
			return compoundTag;
		}));
		tag.put("ReservedSignalBlocks", NBTHelper.writeCompoundList(reservedSignalBlocks, uid -> {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putUUID("Id", uid);
			return compoundTag;
		}));
		tag.put("MigratingPoints", NBTHelper.writeCompoundList(migratingPoints, TrainMigration::write));

		tag.put("Runtime", runtime.write());
		tag.put("Navigation", navigation.write());

		return tag;
	}

	public static Train read(CompoundTag tag, Map<UUID, TrackGraph> trackNetworks) {
		UUID id = tag.getUUID("Id");
		UUID owner = tag.getUUID("Owner");
		UUID graphId = tag.contains("Graph") ? tag.getUUID("Graph") : null;
		TrackGraph graph = graphId == null ? null : trackNetworks.get(graphId);
		List<Carriage> carriages = new ArrayList<>();
		NBTHelper.iterateCompoundList(tag.getList("Carriages", Tag.TAG_COMPOUND),
			c -> carriages.add(Carriage.read(c, graph)));
		List<Integer> carriageSpacing = new ArrayList<>();
		for (int i : tag.getIntArray("CarriageSpacing"))
			carriageSpacing.add(i);
		boolean doubleEnded = tag.getBoolean("DoubleEnded");

		Train train = new Train(id, owner, graph, carriages, carriageSpacing, doubleEnded);

		train.speed = tag.getDouble("Speed");
		train.targetSpeed = tag.getDouble("TargetSpeed");
		train.icon = TrainIconType.byId(new ResourceLocation(tag.getString("IconType")));
		train.name = Component.Serializer.fromJson(tag.getString("Name"));
		train.currentStation = tag.contains("Station") ? tag.getUUID("Station") : null;
		train.currentlyBackwards = tag.getBoolean("Backwards");
		train.heldForAssembly = tag.getBoolean("StillAssembling");
		train.derailed = tag.getBoolean("Derailed");
		train.updateSignalBlocks = tag.getBoolean("UpdateSignals");

		NBTHelper.iterateCompoundList(tag.getList("SignalBlocks", Tag.TAG_COMPOUND), c -> train.occupiedSignalBlocks
			.put(c.getUUID("Id"), c.contains("Boundary") ? c.getUUID("Boundary") : null));
		NBTHelper.iterateCompoundList(tag.getList("ReservedSignalBlocks", Tag.TAG_COMPOUND),
			c -> train.reservedSignalBlocks.add(c.getUUID("Id")));
		NBTHelper.iterateCompoundList(tag.getList("MigratingPoints", Tag.TAG_COMPOUND),
			c -> train.migratingPoints.add(TrainMigration.read(c)));

		train.runtime.read(tag.getCompound("Runtime"));
		train.navigation.read(tag.getCompound("Navigation"), graph);

		if (train.getCurrentStation() != null)
			train.getCurrentStation()
				.reserveFor(train);

		return train;
	}

}
