package com.simibubi.create.content.logistics.trains.entity;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.DimensionPalette;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.track.AbstractBogeyTileEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import static com.simibubi.create.content.logistics.trains.track.AbstractBogeyTileEntity.BOGEY_STYLE_KEY;

public class CarriageBogey {

	public Carriage carriage;
	boolean isLeading;

	public CompoundTag bogeyData;

	AbstractBogeyBlock<?> type;
	boolean upsideDown;
	Couple<TravellingPoint> points;

	LerpedFloat wheelAngle;
	LerpedFloat yaw;
	LerpedFloat pitch;

	public Couple<Vec3> couplingAnchors;

	int derailAngle;

	public CarriageBogey(AbstractBogeyBlock<?> type, boolean upsideDown, CompoundTag bogeyData, TravellingPoint point, TravellingPoint point2) {
		this.type = type;
		if (bogeyData == null || bogeyData.isEmpty())
			bogeyData = this.createBogeyData(); // Prevent Crash When Updating
		this.bogeyData = bogeyData;
		this.upsideDown = type.canBeUpsideDown() && upsideDown;
		point.upsideDown = this.upsideDown;
		point2.upsideDown = this.upsideDown;
		points = Couple.create(point, point2);
		wheelAngle = LerpedFloat.angular();
		yaw = LerpedFloat.angular();
		pitch = LerpedFloat.angular();
		derailAngle = Create.RANDOM.nextInt(60) - 30;
		couplingAnchors = Couple.create(null, null);
	}

	public ResourceKey<Level> getDimension() {
		TravellingPoint leading = leading();
		TravellingPoint trailing = trailing();
		if (leading.edge == null || trailing.edge == null)
			return null;
		if (leading.edge.isInterDimensional() || trailing.edge.isInterDimensional())
			return null;
		ResourceKey<Level> dimension1 = leading.node1.getLocation().dimension;
		ResourceKey<Level> dimension2 = trailing.node1.getLocation().dimension;
		if (dimension1.equals(dimension2))
			return dimension1;
		return null;
	}

	public void updateAngles(CarriageContraptionEntity entity, double distanceMoved) {
		double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());

		float xRot = 0;
		float yRot = 0;

		if (leading().edge == null || carriage.train.derailed) {
			yRot = -90 + entity.yaw - derailAngle;
		} else if (!entity.level.dimension()
			.equals(getDimension())) {
			yRot = -90 + entity.yaw;
			xRot = 0;
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
		TravellingPoint point = points.getFirst();
		point.upsideDown = isUpsideDown();
		return point;
	}

	public TravellingPoint trailing() {
		TravellingPoint point = points.getSecond();
		point.upsideDown = isUpsideDown();
		return point;
	}

	public double getStress() {
		if (getDimension() == null)
			return 0;
		if (carriage.train.derailed)
			return 0;
		return type.getWheelPointSpacing() - leading().getPosition()
			.distanceTo(trailing().getPosition());
	}

	@Nullable
	public Vec3 getAnchorPosition() {
		return getAnchorPosition(false);
	}

	@Nullable
	public Vec3 getAnchorPosition(boolean flipUpsideDown) {
		if (leading().edge == null)
			return null;
		return points.getFirst()
				.getPosition(flipUpsideDown)
				.add(points.getSecond()
						.getPosition(flipUpsideDown))
				.scale(.5);
	}

	public void updateCouplingAnchor(Vec3 entityPos, float entityXRot, float entityYRot, int bogeySpacing,
		float partialTicks, boolean leading) {
		boolean selfUpsideDown = isUpsideDown();
		boolean leadingUpsideDown = carriage.leadingBogey().isUpsideDown();
		Vec3 thisOffset = type.getConnectorAnchorOffset(selfUpsideDown);
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
		if (selfUpsideDown != leadingUpsideDown)
			thisOffset = thisOffset.add(0, selfUpsideDown ? -2 : 2, 0);

		couplingAnchors.set(leading, entityPos.add(thisOffset));
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Type", RegisteredObjects.getKeyOrThrow((Block) type)
			.toString());
		tag.put("Points", points.serializeEach(tp -> tp.write(dimensions)));
		tag.putBoolean("UpsideDown", upsideDown);
		tag.put(BOGEY_STYLE_KEY, bogeyData);
		return tag;
	}

	public static CarriageBogey read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
		ResourceLocation location = new ResourceLocation(tag.getString("Type"));
		AbstractBogeyBlock type = (AbstractBogeyBlock) ForgeRegistries.BLOCKS.getValue(location);
		boolean upsideDown = tag.getBoolean("UpsideDown");
		Couple<TravellingPoint> points = Couple.deserializeEach(tag.getList("Points", Tag.TAG_COMPOUND),
			c -> TravellingPoint.read(c, graph, dimensions));
		CompoundTag data = tag.getCompound(AbstractBogeyTileEntity.BOGEY_DATA_KEY);
		return new CarriageBogey(type, upsideDown, data, points.getFirst(), points.getSecond());
	}

	public BogeyInstance createInstance(MaterialManager materialManager) {
		return this.getStyle().createInstance(this, type.getSize(), materialManager);
	}

	public BogeyStyle getStyle() {
		ResourceLocation location = NBTHelper.readResourceLocation(this.bogeyData, BOGEY_STYLE_KEY);
		return AllBogeyStyles.BOGEY_STYLES.get(location);
	}

	private CompoundTag createBogeyData() {
		CompoundTag nbt = new CompoundTag();
		NBTHelper.writeResourceLocation(nbt, BOGEY_STYLE_KEY, (type != null ? type.getDefaultStyle() : AllBogeyStyles.STANDARD).name);
		return nbt;
	}

	void setLeading() {
		isLeading = true;
	}

	public boolean isUpsideDown() {
		return type.canBeUpsideDown() && upsideDown;
	}
}
