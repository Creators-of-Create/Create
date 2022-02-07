package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.GraphLocation;
import com.simibubi.create.content.logistics.trains.management.ScheduleRuntime;
import com.simibubi.create.content.logistics.trains.management.ScheduleRuntime.State;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Train {

	public static final double acceleration = 0.005f;
	public static final double topSpeed = 1.2f;

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

	List<TrainMigration> migratingPoints;
	public int migrationCooldown;
	public boolean derailed;

	double[] stress;

	public Train(UUID id, UUID owner, TrackGraph graph, List<Carriage> carriages, List<Integer> carriageSpacing) {
		this.id = id;
		this.owner = owner;
		this.graph = graph;
		this.carriages = carriages;
		this.carriageSpacing = carriageSpacing;
		this.icon = TrainIconType.getDefault();
		this.stress = new double[carriageSpacing.size()];
		this.name = Lang.translate("train.unnamed");
		this.status = new TrainStatus(this);

		carriages.forEach(c -> {
			c.setTrain(this);
			Create.RAILWAYS.carriageById.put(c.id, c);
		});

		doubleEnded = carriages.stream()
			.anyMatch(c -> c.contraption.hasBackwardControls());
		navigation = new Navigation(this, graph);
		runtime = new ScheduleRuntime(this);
		heldForAssembly = true;
		migratingPoints = new ArrayList<>();
		currentStation = null;
		manualSteer = SteerDirection.NONE;
	}

	public void tick(Level level) {
		status.tick(level);

		if (graph == null) {
			if (!migratingPoints.isEmpty())
				reattachToTracks(level);
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

		for (int i = 0; i < carriages.size(); i++) {
			Carriage carriage = carriages.get(i);
			if (previousCarriage != null) {
				int target = carriageSpacing.get(i - 1);
				Vec3 leadingAnchor = carriage.leadingBogey().anchorPosition;
				Vec3 trailingAnchor = previousCarriage.trailingBogey().anchorPosition;
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

		for (int index = 0; index < carriages.size(); index++) {
			int i = iterateFromBack ? carriages.size() - 1 - index : index;
			double leadingStress = i == 0 ? 0 : stress[i - 1] * -(iterateFromBack ? trailingModifier : leadingModifier);
			double trailingStress =
				i == stress.length ? 0 : stress[i] * (iterateFromBack ? leadingModifier : trailingModifier);

			Carriage carriage = carriages.get(i);

			TravellingPoint toFollowForward = i == 0 ? null
				: carriages.get(i - 1)
					.getTrailingPoint();

			TravellingPoint toFollowBackward = i == carriages.size() - 1 ? null
				: carriages.get(i + 1)
					.getLeadingPoint();

			Function<TravellingPoint, ITrackSelector> forwardControl =
				toFollowForward == null ? navigation::control : mp -> mp.follow(toFollowForward);
			Function<TravellingPoint, ITrackSelector> backwardControl =
				toFollowBackward == null ? navigation::control : mp -> mp.follow(toFollowBackward);

			double totalStress = leadingStress + trailingStress;
			double actualDistance =
				carriage.travel(level, graph, distance + totalStress, forwardControl, backwardControl);
			blocked |= carriage.blocked;

			if (index == 0) {
				distance = actualDistance;
				collideWithOtherTrains(level, carriage);
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

	private void updateNavigationTarget(double distance) {
		if (navigation.destination != null) {
			boolean recalculate = navigation.distanceToDestination % 100 > 20;
			boolean imminentRecalculate = navigation.distanceToDestination > 5;
			navigation.distanceToDestination -= Math.abs(distance);
			if (recalculate && navigation.distanceToDestination % 100 <= 20
				|| imminentRecalculate && navigation.distanceToDestination <= 5)
				navigation.startNavigation(navigation.destination, false);
		}
	}

	private void tickDerailedSlowdown() {
		speed /= 3f;
		if (Mth.equal(speed, 0))
			speed = 0;
	}

	private void tickPassiveSlowdown() {
		if (!manualTick && navigation.destination == null && speed != 0) {
			if (speed > 0)
				speed = Math.max(speed - acceleration, 0);
			else
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
			if (carriage.hasForwardConductor)
				return true;
		return false;
	}

	public boolean hasBackwardConductor() {
		for (Carriage carriage : carriages)
			if (carriage.hasBackwardConductor)
				return true;
		return false;
	}

	private void collideWithOtherTrains(Level level, Carriage carriage) {
		if (derailed)
			return;
		Collision: for (Train train : Create.RAILWAYS.trains.values()) {
			if (train == this)
				continue;
			Vec3 start = (speed < 0 ? carriage.getTrailingPoint() : carriage.getLeadingPoint()).getPosition();
			Vec3 end = (speed < 0 ? carriage.getLeadingPoint() : carriage.getTrailingPoint()).getPosition();
			Vec3 diff = end.subtract(start);
			Vec3 lastPoint = null;

			for (Carriage otherCarriage : train.carriages) {
				for (boolean betweenBits : Iterate.trueAndFalse) {
					if (betweenBits && lastPoint == null)
						continue;

					Vec3 start2 = otherCarriage.getLeadingPoint()
						.getPosition();
					Vec3 end2 = otherCarriage.getTrailingPoint()
						.getPosition();
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

					double combinedSpeed = Math.abs(speed) + Math.abs(train.speed);
					if (combinedSpeed > .2f) {
						Vec3 v = start.add(normedDiff.scale(intersect[0]));
						level.explode(null, v.x, v.y, v.z, (float) Math.min(3 * combinedSpeed, 5),
							BlockInteraction.NONE);
					}
					crash();
					train.crash();
					break Collision;
				}
			}
		}
	}

	public void crash() {
		navigation.cancelNavigation();
		if (derailed)
			return;
		speed = -Mth.clamp(speed, -.5, .5);
		derailed = true;
		status.crash();
	}

	public boolean disassemble(Direction assemblyDirection, BlockPos pos) {
		for (Carriage carriage : carriages) {
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity == null)
				return false;
			if (!Mth.equal(entity.pitch, 0))
				return false;
			if (!Mth.equal(((entity.yaw % 90) + 360) % 90, 0))
				return false;
		}

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
			Create.RAILWAYS.carriageById.remove(carriage.id);
			CreateClient.RAILWAYS.carriageById.remove(carriage.id);

			offset += carriage.bogeySpacing;

			if (i < carriageSpacing.size())
				offset += carriageSpacing.get(carriageSpacing.size() - i - 1);
		}

		GlobalStation currentStation = getCurrentStation();
		if (currentStation != null)
			currentStation.cancelReservation(this);

		Create.RAILWAYS.trains.remove(id);
		CreateClient.RAILWAYS.trains.remove(id); // TODO Thread breach
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

	}

	public void forEachTravellingPoint(Consumer<TravellingPoint> callback) {
		for (Carriage c : carriages) {
			c.leadingBogey().points.forEach(callback::accept);
			if (c.isOnTwoBogeys())
				c.trailingBogey().points.forEach(callback::accept);
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
			return;
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
		if (currentStation == null)
			return;
		currentStation.trainDeparted(this);
		this.currentStation = null;
	}

	public void arriveAt(GlobalStation station) {
		setCurrentStation(station);
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
		return graph.getStation(currentStation);
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
		if (speed < targetSpeed)
			speed = Math.min(speed + Train.acceleration * accelerationMod, targetSpeed);
		else if (speed > targetSpeed)
			speed = Math.max(speed - Train.acceleration * accelerationMod, targetSpeed);
	}

}
