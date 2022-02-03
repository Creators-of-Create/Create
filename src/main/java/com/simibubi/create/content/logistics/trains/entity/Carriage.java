package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Carriage {

	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	public Train train;
	public CarriageContraption contraption;
	public int bogeySpacing;
	public int id;
	public boolean blocked;

	WeakReference<CarriageContraptionEntity> entity;
	Couple<CarriageBogey> bogeys;

	public Carriage(CarriageBogey bogey1, @Nullable CarriageBogey bogey2, int bogeySpacing) {
		this.bogeySpacing = bogeySpacing;
		this.bogeys = Couple.create(bogey1, bogey2);
		this.entity = new WeakReference<>(null);
		this.id = netIdGenerator.incrementAndGet();
		
		bogey1.carriage = this;
		if (bogey2 != null)
			bogey2.carriage = this;
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public void setContraption(CarriageContraption contraption) {
		this.contraption = contraption;
		contraption.setCarriage(this);
	}

	public double travel(Level level, TrackGraph graph, double distance,
		@Nullable Function<TravellingPoint, ITrackSelector> control) {
		Vec3 leadingAnchor = leadingBogey().anchorPosition;
		Vec3 trailingAnchor = trailingBogey().anchorPosition;
		boolean onTwoBogeys = isOnTwoBogeys();
		double stress = onTwoBogeys ? bogeySpacing - leadingAnchor.distanceTo(trailingAnchor) : 0;
		blocked = false;

		// positive stress: points should move apart
		// negative stress: points should move closer

		double leadingBogeyModifier = 0.5d;
		double trailingBogeyModifier = -0.5d;
		double leadingPointModifier = 0.5d;
		double trailingPointModifier = -0.5d;

		MutableObject<TravellingPoint> previous = new MutableObject<>();
		MutableDouble distanceMoved = new MutableDouble(distance);

		bogeys.forEachWithContext((bogey, firstBogey) -> {
			if (!firstBogey && !onTwoBogeys)
				return;

			double bogeyCorrection = stress * (firstBogey ? leadingBogeyModifier : trailingBogeyModifier);
			double bogeyStress = bogey.getStress();

			bogey.points.forEachWithContext((point, first) -> {
				TravellingPoint prevPoint = previous.getValue();
				ITrackSelector trackSelector =
					prevPoint == null ? control == null ? point.random() : control.apply(point)
						: point.follow(prevPoint);

				double correction = bogeyStress * (first ? leadingPointModifier : trailingPointModifier);
				double toMove = distanceMoved.getValue();
				double moved = point.travel(graph, toMove, trackSelector);
				point.travel(graph, correction + bogeyCorrection, trackSelector);
				blocked |= point.blocked;

				distanceMoved.setValue(moved);
				previous.setValue(point);
			});

			bogey.updateAnchorPosition();
		});

		tickEntity(level);
		return distanceMoved.getValue();
	}

	public void createEntity(Level level) {
		contraption.startMoving(level);
		CarriageContraptionEntity entity = CarriageContraptionEntity.create(level, contraption);
		Vec3 pos = leadingBogey().anchorPosition;
		entity.setPos(pos);
		entity.setInitialOrientation(contraption.getAssemblyDirection()
			.getClockWise());
		level.addFreshEntity(entity);
		this.entity = new WeakReference<>(entity);
	}

	public ChunkPos getChunk() {
		return new ChunkPos(new BlockPos(leadingBogey().anchorPosition));
	}

	protected void tickEntity(Level level) {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity == null) {
			if (CarriageEntityHandler.isActiveChunk(level, getChunk()))
				createEntity(level);
		} else {
			CarriageEntityHandler.validateCarriageEntity(entity);
			if (!entity.isAlive()) {
				this.entity.clear();
				return;
			}
		}

		entity = this.entity.get();
		if (entity == null)
			return;
		if (!entity.level.isClientSide)
			moveEntity(entity);
	}

	public void moveEntity(CarriageContraptionEntity entity) {
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

	public void discardEntity() {
		CarriageContraptionEntity entity = this.entity.get();
		if (entity == null)
			return;
		entity.discard();
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

	public static class CarriageBogey {

		Carriage carriage;
		IBogeyBlock type;
		Couple<TravellingPoint> points;
		Vec3 anchorPosition;

		LerpedFloat wheelAngle;
		LerpedFloat yaw;
		LerpedFloat pitch;

		public Vec3 leadingCouplingAnchor;
		public Vec3 trailingCouplingAnchor;
		
		int derailAngle;
		
		public CarriageBogey(IBogeyBlock type, TravellingPoint point, TravellingPoint point2) {
			this.type = type;
			points = Couple.create(point, point2);
			wheelAngle = LerpedFloat.angular();
			yaw = LerpedFloat.angular();
			pitch = LerpedFloat.angular();
			updateAnchorPosition();
			derailAngle = Create.RANDOM.nextInt(90) - 45;
		}

		public void updateAngles(double distanceMoved) {
			double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
			Vec3 positionVec = leading().getPosition();
			Vec3 coupledVec = trailing().getPosition();
			double diffX = positionVec.x - coupledVec.x;
			double diffY = positionVec.y - coupledVec.y;
			double diffZ = positionVec.z - coupledVec.z;
			float yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
			float xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));
			
			if (carriage.train.derailed)
				yRot += derailAngle;
			
			wheelAngle.setValue((wheelAngle.getValue() - angleDiff) % 360);
			pitch.setValue(xRot);
			yaw.setValue(-yRot);
		}

		public TravellingPoint leading() {
			return points.getFirst();
		}

		public TravellingPoint trailing() {
			return points.getSecond();
		}

		public double getStress() {
			return type.getWheelPointSpacing() - leading().getPosition()
				.distanceTo(trailing().getPosition());
		}

		public void updateAnchorPosition() {
			anchorPosition = points.getFirst()
				.getPosition()
				.add(points.getSecond()
					.getPosition())
				.scale(.5);
		}

		public void updateCouplingAnchor(Vec3 entityPos, float entityXRot, float entityYRot, int bogeySpacing,
			float partialTicks, boolean leading) {
			Vec3 thisOffset = type.getConnectorAnchorOffset();
			thisOffset = thisOffset.multiply(1, 1, leading ? -1 : 1);

			thisOffset = VecHelper.rotate(thisOffset, pitch.getValue(partialTicks), Axis.X);
			thisOffset = VecHelper.rotate(thisOffset, yaw.getValue(partialTicks), Axis.Y);
			thisOffset = VecHelper.rotate(thisOffset, -entityYRot - 90, Axis.Y);
			thisOffset = VecHelper.rotate(thisOffset, entityXRot, Axis.X);
			thisOffset = VecHelper.rotate(thisOffset, -180, Axis.Y);
			thisOffset = thisOffset.add(0, 0, leading ? 0 : -bogeySpacing);
			thisOffset = VecHelper.rotate(thisOffset, 180, Axis.Y);
			thisOffset = VecHelper.rotate(thisOffset, -entityXRot, Axis.X);
			thisOffset = VecHelper.rotate(thisOffset, entityYRot + 90, Axis.Y);

			if (leading)
				leadingCouplingAnchor = entityPos.add(thisOffset);
			else {
				trailingCouplingAnchor = entityPos.add(thisOffset);
			}
		}

	}

}
