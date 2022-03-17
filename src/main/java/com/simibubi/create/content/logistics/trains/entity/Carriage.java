package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;

import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.IEdgePointListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Carriage {

	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	public Train train;
	public int id;
	public boolean blocked;
	public Couple<Boolean> presentConductors;

	public int bogeySpacing;
	public Couple<CarriageBogey> bogeys;

	public Vec3 positionAnchor;
	public Couple<Vec3> rotationAnchors;

	CompoundTag serialisedEntity;
	WeakReference<CarriageContraptionEntity> entity;

	// client
	public boolean pointsInitialised;

	static final int FIRST = 0, MIDDLE = 1, LAST = 2, BOTH = 3;

	public Carriage(CarriageBogey bogey1, @Nullable CarriageBogey bogey2, int bogeySpacing) {
		this.bogeySpacing = bogeySpacing;
		this.bogeys = Couple.create(bogey1, bogey2);
		this.entity = new WeakReference<>(null);
		this.id = netIdGenerator.incrementAndGet();
		this.serialisedEntity = new CompoundTag();
		this.pointsInitialised = false;
		this.rotationAnchors = Couple.create(null, null);

		updateContraptionAnchors();

		bogey1.setLeading();
		bogey1.carriage = this;
		if (bogey2 != null)
			bogey2.carriage = this;
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public void setContraption(Level level, CarriageContraption contraption) {
		CarriageContraptionEntity entity = CarriageContraptionEntity.create(level, contraption);
		entity.setCarriage(this);
		contraption.startMoving(level);
		contraption.onEntityInitialize(level, entity);
		updateContraptionAnchors();
		alignEntity(entity);

		List<Entity> players = new ArrayList<>();
		for (Entity passenger : entity.getPassengers())
			if (!(passenger instanceof Player))
				passenger.remove(RemovalReason.UNLOADED_WITH_PLAYER);
			else
				players.add(passenger);
		for (Entity player : players)
			player.stopRiding();

		serialisedEntity = entity.serializeNBT();
	}

	public double travel(Level level, TrackGraph graph, double distance,
		Function<TravellingPoint, ITrackSelector> forwardControl,
		Function<TravellingPoint, ITrackSelector> backwardControl, int type) {
		boolean onTwoBogeys = isOnTwoBogeys();
		double stress = onTwoBogeys ? bogeySpacing - leadingAnchor().distanceTo(trailingAnchor()) : 0;
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
				double moved =
					point
						.travel(graph, toMove, toMove > 0 ? frontTrackSelector : backTrackSelector,
							toMove > 0 ? atFront ? frontListener : atBack ? backListener : passiveListener
								: atFront ? backListener : atBack ? frontListener : passiveListener,
							point.ignoreTurns());
				blocked |= point.blocked;

				distanceMoved.setValue(moved);
			}
		}

		updateContraptionAnchors();
		manageEntity(level);
		return distanceMoved.getValue();
	}

	public void updateConductors() {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity != null && entity.isAlive())
			presentConductors = entity.checkConductors();
	}

	public void createEntity(Level level) {
		Entity entity = EntityType.loadEntityRecursive(serialisedEntity, level, e -> {
			e.moveTo(positionAnchor);
			return e;
		});

		if (!(entity instanceof CarriageContraptionEntity cce))
			return;

		cce.setGraph(train.graph == null ? null : train.graph.id);
		cce.setCarriage(this);
		cce.syncCarriage();

		this.entity = new WeakReference<>(cce);

		if (level instanceof ServerLevel sl)
			sl.tryAddFreshEntityWithPassengers(entity);
	}

	public void manageEntity(Level level) {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity == null) {
			if (CarriageEntityHandler.isActiveChunk(level, new BlockPos(positionAnchor)))
				createEntity(level);
		} else {
			CarriageEntityHandler.validateCarriageEntity(entity);
			if (!entity.isAlive() || entity.leftTickingChunks) {
				removeAndSaveEntity(entity);
				return;
			}
		}

		entity = this.entity.get();
		if (entity == null)
			return;

		alignEntity(entity);
		entity.syncCarriage();
	}

	private void removeAndSaveEntity(CarriageContraptionEntity entity) {
		serialisedEntity = entity.serializeNBT();

		for (Entity passenger : entity.getPassengers())
			if (!(passenger instanceof Player))
				passenger.discard();
		entity.discard();

		this.entity.clear();
	}

	public void updateContraptionAnchors() {
		CarriageBogey leadingBogey = leadingBogey();
		if (leadingBogey.points.either(t -> t.edge == null))
			return;
		positionAnchor = leadingBogey.getAnchorPosition();
		rotationAnchors = bogeys.mapWithContext((b, first) -> isOnTwoBogeys() ? b.getAnchorPosition()
			: leadingBogey.points.get(first)
				.getPosition());
	}

	public void alignEntity(CarriageContraptionEntity entity) {
		Vec3 positionVec = rotationAnchors.getFirst();
		Vec3 coupledVec = rotationAnchors.getSecond();

		double diffX = positionVec.x - coupledVec.x;
		double diffY = positionVec.y - coupledVec.y;
		double diffZ = positionVec.z - coupledVec.z;

		entity.setPos(positionAnchor);
		entity.prevYaw = entity.yaw;
		entity.prevPitch = entity.pitch;

		entity.yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI) + 180;
		entity.pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI) * -1;

		if (entity.firstPositionUpdate) {
			entity.xo = entity.getX();
			entity.yo = entity.getY();
			entity.zo = entity.getZ();
			entity.prevYaw = entity.yaw;
			entity.prevPitch = entity.pitch;
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

	public Vec3 leadingAnchor() {
		return isOnTwoBogeys() ? rotationAnchors.getFirst() : positionAnchor;
	}

	public Vec3 trailingAnchor() {
		return isOnTwoBogeys() ? rotationAnchors.getSecond() : positionAnchor;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.put("FirstBogey", bogeys.getFirst()
			.write());
		if (isOnTwoBogeys())
			tag.put("SecondBogey", bogeys.getSecond()
				.write());
		tag.putInt("Spacing", bogeySpacing);
		tag.putBoolean("FrontConductor", presentConductors.getFirst());
		tag.putBoolean("BackConductor", presentConductors.getSecond());

		CarriageContraptionEntity entity = this.entity.get();
		if (entity != null)
			serialisedEntity = entity.serializeNBT();

		tag.put("Entity", serialisedEntity.copy());
		tag.put("PositionAnchor", VecHelper.writeNBT(positionAnchor));
		tag.put("RotationAnchors", rotationAnchors.serializeEach(VecHelper::writeNBTCompound));

		return tag;
	}

	public static Carriage read(CompoundTag tag, TrackGraph graph) {
		CarriageBogey bogey1 = CarriageBogey.read(tag.getCompound("FirstBogey"), graph);
		CarriageBogey bogey2 =
			tag.contains("SecondBogey") ? CarriageBogey.read(tag.getCompound("SecondBogey"), graph) : null;

		Carriage carriage = new Carriage(bogey1, bogey2, tag.getInt("Spacing"));

		carriage.presentConductors = Couple.create(tag.getBoolean("FrontConductor"), tag.getBoolean("BackConductor"));
		carriage.serialisedEntity = tag.getCompound("Entity")
			.copy();

		if (carriage.positionAnchor == null) {
			carriage.positionAnchor = VecHelper.readNBT(tag.getList("PositionAnchor", Tag.TAG_DOUBLE));
			carriage.rotationAnchors =
				Couple.deserializeEach(tag.getList("RotationAnchors", Tag.TAG_COMPOUND), VecHelper::readNBTCompound);
		}

		return carriage;
	}

}
