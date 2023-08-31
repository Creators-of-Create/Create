package com.simibubi.create.content.trains.entity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.trains.entity.TravellingPoint.IEdgePointListener;
import com.simibubi.create.content.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class Carriage {

	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	public Train train;
	public int id;
	public boolean blocked;
	public boolean stalled;
	public Couple<Boolean> presentConductors;

	public int bogeySpacing;
	public Couple<CarriageBogey> bogeys;
	public TrainCargoManager storage;

	CompoundTag serialisedEntity;
	Map<Integer, CompoundTag> serialisedPassengers;

	private Map<ResourceKey<Level>, DimensionalCarriageEntity> entities;

	static final int FIRST = 0, MIDDLE = 1, LAST = 2, BOTH = 3;

	public Carriage(CarriageBogey bogey1, @Nullable CarriageBogey bogey2, int bogeySpacing) {
		this.bogeySpacing = bogeySpacing;
		this.bogeys = Couple.create(bogey1, bogey2);
		this.id = netIdGenerator.incrementAndGet();
		this.serialisedEntity = new CompoundTag();
		this.presentConductors = Couple.create(false, false);
		this.serialisedPassengers = new HashMap<>();
		this.entities = new HashMap<>();
		this.storage = new TrainCargoManager();

		bogey1.setLeading();
		bogey1.carriage = this;
		if (bogey2 != null)
			bogey2.carriage = this;
	}

	public boolean isOnIncompatibleTrack() {
		return leadingBogey().type.isOnIncompatibleTrack(this, true)
				|| trailingBogey().type.isOnIncompatibleTrack(this, false);
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public boolean presentInMultipleDimensions() {
		return entities.size() > 1;
	}

	public void setContraption(Level level, CarriageContraption contraption) {
		this.storage = null;
		CarriageContraptionEntity entity = CarriageContraptionEntity.create(level, contraption);
		entity.setCarriage(this);
		contraption.startMoving(level);
		contraption.onEntityInitialize(level, entity);
		updateContraptionAnchors();

		DimensionalCarriageEntity dimensional = getDimensional(level);
		dimensional.alignEntity(entity);
		dimensional.removeAndSaveEntity(entity, true);
	}

	public DimensionalCarriageEntity getDimensional(Level level) {
		return getDimensional(level.dimension());
	}

	public DimensionalCarriageEntity getDimensional(ResourceKey<Level> dimension) {
		return entities.computeIfAbsent(dimension, $ -> new DimensionalCarriageEntity());
	}

	@Nullable
	public DimensionalCarriageEntity getDimensionalIfPresent(ResourceKey<Level> dimension) {
		return entities.get(dimension);
	}

	public double travel(Level level, TrackGraph graph, double distance, TravellingPoint toFollowForward,
		TravellingPoint toFollowBackward, int type) {

		Function<TravellingPoint, ITrackSelector> forwardControl =
			toFollowForward == null ? train.navigation::control : mp -> mp.follow(toFollowForward);
		Function<TravellingPoint, ITrackSelector> backwardControl =
			toFollowBackward == null ? train.navigation::control : mp -> mp.follow(toFollowBackward);

		boolean onTwoBogeys = isOnTwoBogeys();
		double stress = train.derailed ? 0 : onTwoBogeys ? bogeySpacing - getAnchorDiff() : 0;
		blocked = false;

		MutableDouble distanceMoved = new MutableDouble(distance);
		boolean iterateFromBack = distance < 0;

		for (boolean firstBogey : Iterate.trueAndFalse) {
			if (!firstBogey && !onTwoBogeys)
				continue;

			boolean actuallyFirstBogey = !onTwoBogeys || (firstBogey ^ iterateFromBack);
			CarriageBogey bogey = bogeys.get(actuallyFirstBogey);
			double bogeyCorrection = stress * (actuallyFirstBogey ? 0.5d : -0.5d);
			double bogeyStress = bogey.getStress();

			for (boolean firstWheel : Iterate.trueAndFalse) {
				boolean actuallyFirstWheel = firstWheel ^ iterateFromBack;
				TravellingPoint point = bogey.points.get(actuallyFirstWheel);
				TravellingPoint prevPoint = !actuallyFirstWheel ? bogey.points.getFirst()
					: !actuallyFirstBogey && onTwoBogeys ? bogeys.getFirst().points.getSecond() : null;
				TravellingPoint nextPoint = actuallyFirstWheel ? bogey.points.getSecond()
					: actuallyFirstBogey && onTwoBogeys ? bogeys.getSecond().points.getFirst() : null;

				double correction = bogeyStress * (actuallyFirstWheel ? 0.5d : -0.5d);
				double toMove = distanceMoved.getValue();

				ITrackSelector frontTrackSelector =
					prevPoint == null ? forwardControl.apply(point) : point.follow(prevPoint);
				ITrackSelector backTrackSelector =
					nextPoint == null ? backwardControl.apply(point) : point.follow(nextPoint);

				boolean atFront = (type == FIRST || type == BOTH) && actuallyFirstWheel && actuallyFirstBogey;
				boolean atBack =
					(type == LAST || type == BOTH) && !actuallyFirstWheel && (!actuallyFirstBogey || !onTwoBogeys);

				IEdgePointListener frontListener = train.frontSignalListener();
				IEdgePointListener backListener = train.backSignalListener();
				IEdgePointListener passiveListener = point.ignoreEdgePoints();

				toMove += correction + bogeyCorrection;

				ITrackSelector trackSelector = toMove > 0 ? frontTrackSelector : backTrackSelector;
				IEdgePointListener signalListener =
					toMove > 0 ? atFront ? frontListener : atBack ? backListener : passiveListener
						: atFront ? backListener : atBack ? frontListener : passiveListener;

				double moved = point.travel(graph, toMove, trackSelector, signalListener, point.ignoreTurns(), c -> {
					for (DimensionalCarriageEntity dce : entities.values())
						if (c.either(tnl -> tnl.equalsIgnoreDim(dce.pivot)))
							return false;
					if (entities.size() > 1) {
						train.status.doublePortal();
						return true;
					}
					return false;
				});

				blocked |= point.blocked;

				distanceMoved.setValue(moved);
			}
		}

		updateContraptionAnchors();
		manageEntities(level);
		return distanceMoved.getValue();
	}

	public double getAnchorDiff() {
		double diff = 0;
		int entries = 0;

		TravellingPoint leadingPoint = getLeadingPoint();
		TravellingPoint trailingPoint = getTrailingPoint();
		if (leadingPoint.node1 != null && trailingPoint.node1 != null)
			if (!leadingPoint.node1.getLocation().dimension.equals(trailingPoint.node1.getLocation().dimension))
				return bogeySpacing;

		for (DimensionalCarriageEntity dce : entities.values())
			if (dce.leadingAnchor() != null && dce.trailingAnchor() != null) {
				entries++;
				diff += dce.leadingAnchor()
					.distanceTo(dce.trailingAnchor());
			}

		if (entries == 0)
			return bogeySpacing;
		return diff / entries;
	}

	public void updateConductors() {
		if (anyAvailableEntity() == null || entities.size() > 1 || serialisedPassengers.size() > 0)
			return;
		presentConductors.replace($ -> false);
		for (DimensionalCarriageEntity dimensionalCarriageEntity : entities.values()) {
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();
			if (entity != null && entity.isAlive())
				presentConductors.replaceWithParams((current, checked) -> current || checked, entity.checkConductors());
		}
	}

	private Set<ResourceKey<Level>> currentlyTraversedDimensions = new HashSet<>();

	public void manageEntities(Level level) {
		currentlyTraversedDimensions.clear();

		bogeys.forEach(cb -> {
			if (cb == null)
				return;
			cb.points.forEach(tp -> {
				if (tp.node1 == null)
					return;
				currentlyTraversedDimensions.add(tp.node1.getLocation().dimension);
			});
		});

		for (Iterator<Entry<ResourceKey<Level>, DimensionalCarriageEntity>> iterator = entities.entrySet()
			.iterator(); iterator.hasNext();) {
			Entry<ResourceKey<Level>, DimensionalCarriageEntity> entry = iterator.next();

			boolean discard =
				!currentlyTraversedDimensions.isEmpty() && !currentlyTraversedDimensions.contains(entry.getKey());

			MinecraftServer server = level.getServer();
			if (server == null)
				continue;
			ServerLevel currentLevel = server.getLevel(entry.getKey());
			if (currentLevel == null)
				continue;

			DimensionalCarriageEntity dimensionalCarriageEntity = entry.getValue();
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();

			if (entity == null) {
				if (discard)
					iterator.remove();
				else if (dimensionalCarriageEntity.positionAnchor != null && CarriageEntityHandler
					.isActiveChunk(currentLevel, BlockPos.containing(dimensionalCarriageEntity.positionAnchor)))
					dimensionalCarriageEntity.createEntity(currentLevel, anyAvailableEntity() == null);

			} else {
				if (discard) {
					discard = dimensionalCarriageEntity.discardTicks > 3;
					dimensionalCarriageEntity.discardTicks++;
				} else
					dimensionalCarriageEntity.discardTicks = 0;

				CarriageEntityHandler.validateCarriageEntity(entity);
				if (!entity.isAlive() || entity.leftTickingChunks || discard) {
					dimensionalCarriageEntity.removeAndSaveEntity(entity, discard);
					if (discard)
						iterator.remove();
					continue;
				}
			}

			entity = dimensionalCarriageEntity.entity.get();
			if (entity != null && dimensionalCarriageEntity.positionAnchor != null) {
				dimensionalCarriageEntity.alignEntity(entity);
				entity.syncCarriage();
			}
		}

	}

	public void updateContraptionAnchors() {
		CarriageBogey leadingBogey = leadingBogey();
		if (leadingBogey.points.either(t -> t.edge == null))
			return;
		CarriageBogey trailingBogey = trailingBogey();
		if (trailingBogey.points.either(t -> t.edge == null))
			return;

		ResourceKey<Level> leadingBogeyDim = leadingBogey.getDimension();
		ResourceKey<Level> trailingBogeyDim = trailingBogey.getDimension();
		double leadingWheelSpacing = leadingBogey.type.getWheelPointSpacing();
		double trailingWheelSpacing = trailingBogey.type.getWheelPointSpacing();

		boolean leadingUpsideDown = leadingBogey.isUpsideDown();
		boolean trailingUpsideDown = trailingBogey.isUpsideDown();

		for (boolean leading : Iterate.trueAndFalse) {
			TravellingPoint point = leading ? getLeadingPoint() : getTrailingPoint();
			TravellingPoint otherPoint = !leading ? getLeadingPoint() : getTrailingPoint();
			ResourceKey<Level> dimension = point.node1.getLocation().dimension;
			ResourceKey<Level> otherDimension = otherPoint.node1.getLocation().dimension;

			if (dimension.equals(otherDimension) && leading) {
				getDimensional(dimension).discardPivot();
				continue;
			}

			DimensionalCarriageEntity dce = getDimensional(dimension);

			dce.positionAnchor = dimension.equals(leadingBogeyDim) ? leadingBogey.getAnchorPosition()
				: pivoted(dce, dimension, point,
					leading ? leadingWheelSpacing / 2 : bogeySpacing + trailingWheelSpacing / 2,
					leadingUpsideDown, trailingUpsideDown);

			boolean backAnchorFlip = trailingBogey.isUpsideDown() ^ leadingBogey.isUpsideDown();

			if (isOnTwoBogeys()) {
				dce.rotationAnchors.setFirst(dimension.equals(leadingBogeyDim) ? leadingBogey.getAnchorPosition()
					: pivoted(dce, dimension, point,
						leading ? leadingWheelSpacing / 2 : bogeySpacing + trailingWheelSpacing / 2,
						leadingUpsideDown, trailingUpsideDown));
				dce.rotationAnchors.setSecond(dimension.equals(trailingBogeyDim) ? trailingBogey.getAnchorPosition(backAnchorFlip)
					: pivoted(dce, dimension, point,
						leading ? leadingWheelSpacing / 2 + bogeySpacing : trailingWheelSpacing / 2,
						leadingUpsideDown, trailingUpsideDown));

			} else {
				if (dimension.equals(otherDimension)) {
					dce.rotationAnchors = leadingBogey.points.map(tp -> tp.getPosition(train.graph));
				} else {
					dce.rotationAnchors.setFirst(leadingBogey.points.getFirst() == point
						? point.getPosition(train.graph)
						: pivoted(dce, dimension, point, leadingWheelSpacing, leadingUpsideDown, trailingUpsideDown));
					dce.rotationAnchors.setSecond(leadingBogey.points.getSecond() == point
						? point.getPosition(train.graph)
						: pivoted(dce, dimension, point, leadingWheelSpacing, leadingUpsideDown, trailingUpsideDown));
				}
			}

			int prevmin = dce.minAllowedLocalCoord();
			int prevmax = dce.maxAllowedLocalCoord();

			dce.updateCutoff(leading);

			if (prevmin != dce.minAllowedLocalCoord() || prevmax != dce.maxAllowedLocalCoord()) {
				dce.updateRenderedCutoff();
				dce.updatePassengerLoadout();
			}
		}

	}

	private Vec3 pivoted(DimensionalCarriageEntity dce, ResourceKey<Level> dimension, TravellingPoint start,
		double offset, boolean leadingUpsideDown, boolean trailingUpsideDown) {
		if (train.graph == null)
			return dce.pivot == null ? null : dce.pivot.getLocation();
		TrackNodeLocation pivot = dce.findPivot(dimension, start == getLeadingPoint());
		if (pivot == null)
			return null;
		boolean flipped = start != getLeadingPoint() && (leadingUpsideDown != trailingUpsideDown);
		Vec3 startVec = start.getPosition(train.graph, flipped);
		Vec3 portalVec = pivot.getLocation()
			.add(0, leadingUpsideDown ? -1.0 : 1.0, 0);
		return VecHelper.lerp((float) (offset / startVec.distanceTo(portalVec)), startVec, portalVec);
	}

	public void alignEntity(Level level) {
		DimensionalCarriageEntity dimensionalCarriageEntity = entities.get(level.dimension());
		if (dimensionalCarriageEntity != null) {
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();
			if (entity != null)
				dimensionalCarriageEntity.alignEntity(entity);
		}
	}

	public TravellingPoint getLeadingPoint() {
		return leadingBogey().leading();
	}

	public TravellingPoint getTrailingPoint() {
		return trailingBogey().trailing();
	}

	public CarriageBogey leadingBogey() {
		return bogeys.getFirst();
	}

	public CarriageBogey trailingBogey() {
		return isOnTwoBogeys() ? bogeys.getSecond() : leadingBogey();
	}

	public boolean isOnTwoBogeys() {
		return bogeys.getSecond() != null;
	}

	public CarriageContraptionEntity anyAvailableEntity() {
		for (DimensionalCarriageEntity dimensionalCarriageEntity : entities.values()) {
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();
			if (entity != null)
				return entity;
		}
		return null;
	}

	public void forEachPresentEntity(Consumer<CarriageContraptionEntity> callback) {
		for (DimensionalCarriageEntity dimensionalCarriageEntity : entities.values()) {
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();
			if (entity != null)
				callback.accept(entity);
		}
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		tag.put("FirstBogey", bogeys.getFirst()
			.write(dimensions));
		if (isOnTwoBogeys())
			tag.put("SecondBogey", bogeys.getSecond()
				.write(dimensions));
		tag.putInt("Spacing", bogeySpacing);
		tag.putBoolean("FrontConductor", presentConductors.getFirst());
		tag.putBoolean("BackConductor", presentConductors.getSecond());
		tag.putBoolean("Stalled", stalled);

		Map<Integer, CompoundTag> passengerMap = new HashMap<>();

		for (DimensionalCarriageEntity dimensionalCarriageEntity : entities.values()) {
			CarriageContraptionEntity entity = dimensionalCarriageEntity.entity.get();
			if (entity == null)
				continue;
			serialize(entity);
			Contraption contraption = entity.getContraption();
			if (contraption == null)
				continue;
			Map<UUID, Integer> mapping = contraption.getSeatMapping();
			for (Entity passenger : entity.getPassengers())
				if (mapping.containsKey(passenger.getUUID()))
					passengerMap.put(mapping.get(passenger.getUUID()), passenger.serializeNBT());
		}

		tag.put("Entity", serialisedEntity.copy());

		CompoundTag passengerTag = new CompoundTag();
		passengerMap.putAll(serialisedPassengers);
		passengerMap.forEach((seat, nbt) -> passengerTag.put("Seat" + seat, nbt.copy()));
		tag.put("Passengers", passengerTag);

		tag.put("EntityPositioning", NBTHelper.writeCompoundList(entities.entrySet(), e -> {
			CompoundTag c = e.getValue()
				.write();
			c.putInt("Dim", dimensions.encode(e.getKey()));
			return c;
		}));

		return tag;
	}

	private void serialize(Entity entity) {
		serialisedEntity = entity.serializeNBT();
		serialisedEntity.remove("Passengers");
		serialisedEntity.getCompound("Contraption")
			.remove("Passengers");
	}

	public static Carriage read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
		CarriageBogey bogey1 = CarriageBogey.read(tag.getCompound("FirstBogey"), graph, dimensions);
		CarriageBogey bogey2 =
			tag.contains("SecondBogey") ? CarriageBogey.read(tag.getCompound("SecondBogey"), graph, dimensions) : null;

		Carriage carriage = new Carriage(bogey1, bogey2, tag.getInt("Spacing"));

		carriage.stalled = tag.getBoolean("Stalled");
		carriage.presentConductors = Couple.create(tag.getBoolean("FrontConductor"), tag.getBoolean("BackConductor"));
		carriage.serialisedEntity = tag.getCompound("Entity")
			.copy();

		NBTHelper.iterateCompoundList(tag.getList("EntityPositioning", Tag.TAG_COMPOUND),
			c -> carriage.getDimensional(dimensions.decode(c.getInt("Dim")))
				.read(c));

		CompoundTag passengersTag = tag.getCompound("Passengers");
		passengersTag.getAllKeys()
			.forEach(key -> carriage.serialisedPassengers.put(Integer.valueOf(key.substring(4)),
				passengersTag.getCompound(key)));

		return carriage;
	}

	private TravellingPoint portalScout = new TravellingPoint();

	public class DimensionalCarriageEntity {
		public Vec3 positionAnchor;
		public Couple<Vec3> rotationAnchors;
		public WeakReference<CarriageContraptionEntity> entity;

		public TrackNodeLocation pivot;
		int discardTicks;

		// 0 == whole, 0..1 = fading out, -1..0 = fading in
		public float cutoff;

		// client
		public boolean pointsInitialised;

		public DimensionalCarriageEntity() {
			this.entity = new WeakReference<>(null);
			this.rotationAnchors = Couple.create(null, null);
			this.pointsInitialised = false;
		}

		public void discardPivot() {
			int prevmin = minAllowedLocalCoord();
			int prevmax = maxAllowedLocalCoord();

			cutoff = 0;
			pivot = null;

			if ((!serialisedPassengers.isEmpty() && entity.get() != null) || prevmin != minAllowedLocalCoord()
				|| prevmax != maxAllowedLocalCoord()) {
				updatePassengerLoadout();
				updateRenderedCutoff();
			}
		}

		public void updateCutoff(boolean leadingIsCurrent) {
			Vec3 leadingAnchor = rotationAnchors.getFirst();
			Vec3 trailingAnchor = rotationAnchors.getSecond();

			if (leadingAnchor == null || trailingAnchor == null)
				return;
			if (pivot == null) {
				cutoff = 0;
				return;
			}

			Vec3 pivotLoc = pivot.getLocation()
				.add(0, 1, 0);

			double leadingSpacing = leadingBogey().type.getWheelPointSpacing() / 2;
			double trailingSpacing = trailingBogey().type.getWheelPointSpacing() / 2;
			double anchorSpacing = leadingSpacing + bogeySpacing + trailingSpacing;

			if (isOnTwoBogeys()) {
				Vec3 diff = trailingAnchor.subtract(leadingAnchor)
					.normalize();
				trailingAnchor = trailingAnchor.add(diff.scale(trailingSpacing));
				leadingAnchor = leadingAnchor.add(diff.scale(-leadingSpacing));
			}

			double leadingDiff = leadingAnchor.distanceTo(pivotLoc);
			double trailingDiff = trailingAnchor.distanceTo(pivotLoc);

			leadingDiff /= anchorSpacing;
			trailingDiff /= anchorSpacing;

			if (leadingIsCurrent && leadingDiff > trailingDiff && leadingDiff > 1)
				cutoff = 0;
			else if (leadingIsCurrent && leadingDiff < trailingDiff && trailingDiff > 1)
				cutoff = 1;
			else if (!leadingIsCurrent && leadingDiff > trailingDiff && leadingDiff > 1)
				cutoff = -1;
			else if (!leadingIsCurrent && leadingDiff < trailingDiff && trailingDiff > 1)
				cutoff = 0;
			else
				cutoff = (float) Mth.clamp(1 - (leadingIsCurrent ? leadingDiff : trailingDiff), 0, 1)
					* (leadingIsCurrent ? 1 : -1);
		}

		public TrackNodeLocation findPivot(ResourceKey<Level> dimension, boolean leading) {
			if (pivot != null)
				return pivot;

			TravellingPoint start = leading ? getLeadingPoint() : getTrailingPoint();
			TravellingPoint end = !leading ? getLeadingPoint() : getTrailingPoint();

			portalScout.node1 = start.node1;
			portalScout.node2 = start.node2;
			portalScout.edge = start.edge;
			portalScout.position = start.position;

			ITrackSelector trackSelector = portalScout.follow(end);
			int distance = bogeySpacing + 10;
			int direction = leading ? -1 : 1;

			portalScout.travel(train.graph, direction * distance, trackSelector, portalScout.ignoreEdgePoints(),
				portalScout.ignoreTurns(), nodes -> {
					for (boolean b : Iterate.trueAndFalse)
						if (nodes.get(b).dimension.equals(dimension))
							pivot = nodes.get(b);
					return true;
				});

			return pivot;
		}

		public CompoundTag write() {
			CompoundTag tag = new CompoundTag();
			tag.putFloat("Cutoff", cutoff);
			tag.putInt("DiscardTicks", discardTicks);
			storage.write(tag, false);
			if (pivot != null)
				tag.put("Pivot", pivot.write(null));
			if (positionAnchor != null)
				tag.put("PositionAnchor", VecHelper.writeNBT(positionAnchor));
			if (rotationAnchors.both(Objects::nonNull))
				tag.put("RotationAnchors", rotationAnchors.serializeEach(VecHelper::writeNBTCompound));
			return tag;
		}

		public void read(CompoundTag tag) {
			cutoff = tag.getFloat("Cutoff");
			discardTicks = tag.getInt("DiscardTicks");
			storage.read(tag, null, false);
			if (tag.contains("Pivot"))
				pivot = TrackNodeLocation.read(tag.getCompound("Pivot"), null);
			if (positionAnchor != null)
				return;
			if (tag.contains("PositionAnchor"))
				positionAnchor = VecHelper.readNBT(tag.getList("PositionAnchor", Tag.TAG_DOUBLE));
			if (tag.contains("RotationAnchors"))
				rotationAnchors = Couple.deserializeEach(tag.getList("RotationAnchors", Tag.TAG_COMPOUND),
					VecHelper::readNBTCompound);
		}

		public Vec3 leadingAnchor() {
			return isOnTwoBogeys() ? rotationAnchors.getFirst() : positionAnchor;
		}

		public Vec3 trailingAnchor() {
			return isOnTwoBogeys() ? rotationAnchors.getSecond() : positionAnchor;
		}

		public int minAllowedLocalCoord() {
			if (cutoff <= 0)
				return Integer.MIN_VALUE;
			if (cutoff >= 1)
				return Integer.MAX_VALUE;
			return Mth.floor(-bogeySpacing + -1 + (2 + bogeySpacing) * cutoff);
		}

		public int maxAllowedLocalCoord() {
			if (cutoff >= 0)
				return Integer.MAX_VALUE;
			if (cutoff <= -1)
				return Integer.MIN_VALUE;
			return Mth.ceil(-bogeySpacing + -1 + (2 + bogeySpacing) * (cutoff + 1));
		}

		public void updatePassengerLoadout() {
			Entity entity = this.entity.get();
			if (!(entity instanceof CarriageContraptionEntity cce))
				return;
			if (!(entity.level() instanceof ServerLevel sLevel))
				return;

			Set<Integer> loadedPassengers = new HashSet<>();
			int min = minAllowedLocalCoord();
			int max = maxAllowedLocalCoord();

			for (Entry<Integer, CompoundTag> entry : serialisedPassengers.entrySet()) {
				Integer seatId = entry.getKey();
				List<BlockPos> seats = cce.getContraption()
					.getSeats();
				if (seatId >= seats.size())
					continue;

				BlockPos localPos = seats.get(seatId);
				if (!cce.isLocalCoordWithin(localPos, min, max))
					continue;

				CompoundTag tag = entry.getValue();
				Entity passenger = null;

				if (tag.contains("PlayerPassenger")) {
					passenger = sLevel.getServer()
						.getPlayerList()
						.getPlayer(tag.getUUID("PlayerPassenger"));

				} else {
					passenger = EntityType.loadEntityRecursive(tag, entity.level(), e -> {
						e.moveTo(positionAnchor);
						return e;
					});
					if (passenger != null)
						sLevel.tryAddFreshEntityWithPassengers(passenger);
				}

				if (passenger != null) {
					ResourceKey<Level> passengerDimension = passenger.level().dimension();
					if (!passengerDimension.equals(sLevel.dimension()) && passenger instanceof ServerPlayer sp)
						continue;
					cce.addSittingPassenger(passenger, seatId);
				}

				loadedPassengers.add(seatId);
			}

			loadedPassengers.forEach(serialisedPassengers::remove);

			Map<UUID, Integer> mapping = cce.getContraption()
				.getSeatMapping();
			for (Entity passenger : entity.getPassengers()) {
				BlockPos localPos = cce.getContraption()
					.getSeatOf(passenger.getUUID());
				if (cce.isLocalCoordWithin(localPos, min, max))
					continue;
				if (!mapping.containsKey(passenger.getUUID()))
					continue;

				Integer seat = mapping.get(passenger.getUUID());
				if ((passenger instanceof ServerPlayer sp)) {
					dismountPlayer(sLevel, sp, seat, true);
					continue;
				}

				serialisedPassengers.put(seat, passenger.serializeNBT());
				passenger.discard();
			}

		}

		private void dismountPlayer(ServerLevel sLevel, ServerPlayer sp, Integer seat, boolean capture) {
			if (!capture) {
				sp.stopRiding();
				return;
			}

			CompoundTag tag = new CompoundTag();
			tag.putUUID("PlayerPassenger", sp.getUUID());
			serialisedPassengers.put(seat, tag);
			sp.stopRiding();
			sp.getPersistentData()
				.remove("ContraptionDismountLocation");

			for (Entry<ResourceKey<Level>, DimensionalCarriageEntity> other : entities.entrySet()) {
				DimensionalCarriageEntity otherDce = other.getValue();
				if (otherDce == this)
					continue;
				if (sp.level().dimension()
					.equals(other.getKey()))
					continue;
				Vec3 loc = otherDce.pivot == null ? otherDce.positionAnchor : otherDce.pivot.getLocation();
				if (loc == null)
					continue;
				ServerLevel level = sLevel.getServer()
					.getLevel(other.getKey());
				sp.teleportTo(level, loc.x, loc.y, loc.z, sp.getYRot(), sp.getXRot());
				sp.setPortalCooldown();
				AllAdvancements.TRAIN_PORTAL.awardTo(sp);
			}
		}

		public void updateRenderedCutoff() {
			Entity entity = this.entity.get();
			if (!(entity instanceof CarriageContraptionEntity cce))
				return;
			Contraption contraption = cce.getContraption();
			if (!(contraption instanceof CarriageContraption cc))
				return;
			cc.portalCutoffMin = minAllowedLocalCoord();
			cc.portalCutoffMax = maxAllowedLocalCoord();
			if (!entity.level().isClientSide())
				return;
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> invalidate(cce));
		}

		@OnlyIn(Dist.CLIENT)
		private void invalidate(CarriageContraptionEntity entity) {
			entity.getContraption().deferInvalidate = true;
			entity.updateRenderedPortalCutoff();
		}

		private void createEntity(Level level, boolean loadPassengers) {
			Entity entity = EntityType.create(serialisedEntity, level)
				.orElse(null);

			if (!(entity instanceof CarriageContraptionEntity cce)) {
				train.invalid = true;
				return;
			}

			entity.moveTo(positionAnchor);
			this.entity = new WeakReference<>(cce);

			cce.setCarriage(Carriage.this);
			cce.syncCarriage();

			if (level instanceof ServerLevel sl)
				sl.addFreshEntity(entity);

			updatePassengerLoadout();
		}

		private void removeAndSaveEntity(CarriageContraptionEntity entity, boolean portal) {
			Contraption contraption = entity.getContraption();
			if (contraption != null) {
				Map<UUID, Integer> mapping = contraption.getSeatMapping();
				for (Entity passenger : entity.getPassengers()) {
					if (!mapping.containsKey(passenger.getUUID()))
						continue;

					Integer seat = mapping.get(passenger.getUUID());

					if (passenger instanceof ServerPlayer sp) {
						dismountPlayer(sp.serverLevel(), sp, seat, portal);
						continue;
					}

					serialisedPassengers.put(seat, passenger.serializeNBT());
				}
			}

			for (Entity passenger : entity.getPassengers())
				if (!(passenger instanceof Player))
					passenger.discard();

			serialize(entity);
			entity.discard();
			this.entity.clear();
		}

		public void alignEntity(CarriageContraptionEntity entity) {
			if (rotationAnchors.either(Objects::isNull))
				return;

			Vec3 positionVec = rotationAnchors.getFirst();
			Vec3 coupledVec = rotationAnchors.getSecond();

			double diffX = positionVec.x - coupledVec.x;
			double diffY = positionVec.y - coupledVec.y;
			double diffZ = positionVec.z - coupledVec.z;

			entity.prevYaw = entity.yaw;
			entity.prevPitch = entity.pitch;

			if (!entity.level().isClientSide()) {
				Vec3 lookahead = positionAnchor.add(positionAnchor.subtract(entity.position())
					.normalize()
					.scale(16));

				for (Entity e : entity.getPassengers()) {
					if (!(e instanceof Player))
						continue;
					if (e.distanceToSqr(entity) > 32 * 32)
						continue;
					if (CarriageEntityHandler.isActiveChunk(entity.level(), BlockPos.containing(lookahead)))
						break;
					train.carriageWaitingForChunks = id;
					return;
				}

				if (entity.getPassengers()
					.stream()
					.anyMatch(p -> p instanceof Player)
					) {
				}

				if (train.carriageWaitingForChunks == id)
					train.carriageWaitingForChunks = -1;

				entity.setServerSidePrevPosition();
			}

			entity.setPos(positionAnchor);
			entity.yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI) + 180;
			entity.pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI) * -1;

			if (!entity.firstPositionUpdate)
				return;

			entity.xo = entity.getX();
			entity.yo = entity.getY();
			entity.zo = entity.getZ();
			entity.prevYaw = entity.yaw;
			entity.prevPitch = entity.pitch;
		}
	}

}
