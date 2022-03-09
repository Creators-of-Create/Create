package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;

import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ISignalBoundaryListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Carriage {

	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	public Train train;
	public int id;
	public boolean blocked;
	public Couple<Boolean> presentConductors;

	public int bogeySpacing;
	Couple<CarriageBogey> bogeys;

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
		for (CarriageBogey carriageBogey : bogeys)
			if (carriageBogey != null)
				carriageBogey.updateAnchorPosition();
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
		Vec3 leadingAnchor = leadingBogey().anchorPosition;
		Vec3 trailingAnchor = trailingBogey().anchorPosition;
		boolean onTwoBogeys = isOnTwoBogeys();
		double stress = onTwoBogeys ? bogeySpacing - leadingAnchor.distanceTo(trailingAnchor) : 0;
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

				ISignalBoundaryListener frontListener = train.frontSignalListener();
				ISignalBoundaryListener backListener = train.backSignalListener();
				ISignalBoundaryListener passiveListener = point.ignoreSignals();

				toMove += correction + bogeyCorrection;
				double moved = point.travel(graph, toMove, toMove > 0 ? frontTrackSelector : backTrackSelector,
					toMove > 0 ? atFront ? frontListener : atBack ? backListener : passiveListener
						: atFront ? backListener : atBack ? frontListener : passiveListener);
				blocked |= point.blocked;

				distanceMoved.setValue(moved);
			}

			bogey.updateAnchorPosition();
		}

		double actualMovement = distanceMoved.getValue();
		manageEntity(level, actualMovement);
		return actualMovement;
	}

	public void updateConductors() {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity != null && entity.isAlive())
			presentConductors = entity.checkConductors();
	}

	public void createEntity(Level level) {
		Entity entity = EntityType.loadEntityRecursive(serialisedEntity, level, e -> {
			level.addFreshEntity(e);
			return e;
		});
		if (!(entity instanceof CarriageContraptionEntity cce))
			return;

		Vec3 pos = leadingBogey().anchorPosition;
		cce.setPos(pos);
		cce.setCarriage(this);
		cce.setGraph(train.graph == null ? null : train.graph.id);
		cce.syncCarriage();
		this.entity = new WeakReference<>(cce);
	}

	public ChunkPos getChunk() {
		return new ChunkPos(new BlockPos(leadingBogey().anchorPosition));
	}

	protected void manageEntity(Level level, double actualMovement) {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity == null) {
			if (CarriageEntityHandler.isActiveChunk(level, getChunk()))
				createEntity(level);
		} else {
			CarriageEntityHandler.validateCarriageEntity(entity);
			if (!entity.isAlive() || entity.leftTickingChunks) {
				for (Entity passenger : entity.getPassengers())
					if (!(passenger instanceof Player))
						passenger.remove(RemovalReason.UNLOADED_WITH_PLAYER);
				serialisedEntity = entity.serializeNBT();
				entity.discard();
				this.entity.clear();
				return;
			}
		}

		entity = this.entity.get();
		if (entity == null)
			return;

		alignEntity(entity);
		entity.syncCarriage();
	}

	public void alignEntity(CarriageContraptionEntity entity) {
		Vec3 positionVec = isOnTwoBogeys() ? leadingBogey().anchorPosition
			: leadingBogey().leading()
				.getPosition();
		Vec3 coupledVec = isOnTwoBogeys() ? trailingBogey().anchorPosition
			: leadingBogey().trailing()
				.getPosition();

		double diffX = positionVec.x - coupledVec.x;
		double diffY = positionVec.y - coupledVec.y;
		double diffZ = positionVec.z - coupledVec.z;

		entity.setPos(leadingBogey().anchorPosition);
		entity.prevYaw = entity.yaw;
		entity.prevPitch = entity.pitch;
		entity.yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI) + 180;
		entity.pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI) * -1;
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

}
