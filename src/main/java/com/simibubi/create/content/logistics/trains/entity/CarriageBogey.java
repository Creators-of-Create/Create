package com.simibubi.create.content.logistics.trains.entity;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class CarriageBogey {

	public Carriage carriage;

	IBogeyBlock type;
	Couple<TravellingPoint> points;

	LerpedFloat wheelAngle;
	LerpedFloat yaw;
	LerpedFloat pitch;

	public Couple<Vec3> couplingAnchors;

	int derailAngle;

	public CarriageBogey(IBogeyBlock type, TravellingPoint point, TravellingPoint point2) {
		this.type = type;
		points = Couple.create(point, point2);
		wheelAngle = LerpedFloat.angular();
		yaw = LerpedFloat.angular();
		pitch = LerpedFloat.angular();
		derailAngle = Create.RANDOM.nextInt(60) - 30;
		couplingAnchors = Couple.create(null, null);
	}

	public void updateAngles(CarriageContraptionEntity entity, double distanceMoved) {
		double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());

		float xRot = 0;
		float yRot = 0;

		if (leading().edge == null || carriage.train.derailed) {
			yRot = -90 + entity.yaw - derailAngle;
		} else {
			Vec3 positionVec = leading().getPosition();
			Vec3 coupledVec = trailing().getPosition();
			double diffX = positionVec.x - coupledVec.x;
			double diffY = positionVec.y - coupledVec.y;
			double diffZ = positionVec.z - coupledVec.z;
			yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
			xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));
		}

		double newWheelAngle = (wheelAngle.getValue() - angleDiff) % 360;

		for (boolean twice : Iterate.trueAndFalse) {
			if (twice && !entity.firstPositionUpdate)
				continue;
			wheelAngle.setValue(newWheelAngle);
			pitch.setValue(xRot);
			yaw.setValue(-yRot);
		}
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

	@Nullable
	public Vec3 getAnchorPosition() {
		if (leading().edge == null)
			return null;
		return points.getFirst()
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

		couplingAnchors.set(leading, entityPos.add(thisOffset));
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Type", ((Block) type).getRegistryName()
			.toString());
		tag.put("Points", points.serializeEach(TravellingPoint::write));
		return tag;
	}

	public static CarriageBogey read(CompoundTag tag, TrackGraph graph) {
		ResourceLocation location = new ResourceLocation(tag.getString("Type"));
		IBogeyBlock type = (IBogeyBlock) ForgeRegistries.BLOCKS.getValue(location);
		Couple<TravellingPoint> points =
			Couple.deserializeEach(tag.getList("Points", Tag.TAG_COMPOUND), c -> TravellingPoint.read(c, graph));
		CarriageBogey carriageBogey = new CarriageBogey(type, points.getFirst(), points.getSecond());
		return carriageBogey;
	}

}