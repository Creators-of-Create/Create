package com.simibubi.create.content.logistics.trains.entity;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CarriageBogey {

	public Carriage carriage;

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
		if (points.getFirst().node1 == null)
			return;
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
		else
			trailingCouplingAnchor = entityPos.add(thisOffset);
	}

}