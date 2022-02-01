package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.repack.joml.Math;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BezierConnection {

	public Couple<BlockPos> tePositions;
	public Couple<Boolean> trackEnds;
	public Couple<Vec3> starts;
	public Couple<Vec3> axes;
	public Couple<Vec3> normals;
	public boolean primary;

	// runtime

	Vec3 finish1;
	Vec3 finish2;
	private boolean resolved;
	private double length;
	private float[] stepLUT;
	private int segments;

	private double radius;
	private double handleLength;

	public BezierConnection(Couple<BlockPos> positions, Couple<Vec3> starts, Couple<Vec3> axes, Couple<Vec3> normals,
		Couple<Boolean> targets, boolean primary) {
		tePositions = positions;
		this.starts = starts;
		this.axes = axes;
		this.normals = normals;
		this.trackEnds = targets;
		this.primary = primary;
		resolved = false;
	}

	public BezierConnection secondary() {
		return new BezierConnection(tePositions.swap(), starts.swap(), axes.swap(), normals.swap(), trackEnds.swap(),
			false);
	}

	public BezierConnection(CompoundTag compound) {
		this(Couple.deserializeEach(compound.getList("Positions", Tag.TAG_COMPOUND), NbtUtils::readBlockPos),
			Couple.deserializeEach(compound.getList("Starts", Tag.TAG_COMPOUND), VecHelper::readNBTCompound),
			Couple.deserializeEach(compound.getList("Axes", Tag.TAG_COMPOUND), VecHelper::readNBTCompound),
			Couple.deserializeEach(compound.getList("Normals", Tag.TAG_COMPOUND), VecHelper::readNBTCompound),
			Couple.create(compound.getBoolean("TrackEnd1"), compound.getBoolean("TrackEnd2")),
			compound.getBoolean("Primary"));
	}

	public CompoundTag write() {
		CompoundTag compound = new CompoundTag();
		compound.putBoolean("Primary", primary);
		compound.putBoolean("TrackEnd1", trackEnds.getFirst());
		compound.putBoolean("TrackEnd2", trackEnds.getSecond());
		compound.put("Positions", tePositions.serializeEach(NbtUtils::writeBlockPos));
		compound.put("Starts", starts.serializeEach(VecHelper::writeNBTCompound));
		compound.put("Axes", axes.serializeEach(VecHelper::writeNBTCompound));
		compound.put("Normals", normals.serializeEach(VecHelper::writeNBTCompound));
		return compound;
	}

	public BezierConnection(FriendlyByteBuf buffer) {
		this(Couple.create(buffer::readBlockPos), Couple.create(() -> VecHelper.read(buffer)),
			Couple.create(() -> VecHelper.read(buffer)), Couple.create(() -> VecHelper.read(buffer)),
			Couple.create(buffer::readBoolean), buffer.readBoolean());
	}

	public void write(FriendlyByteBuf buffer) {
		tePositions.forEach(buffer::writeBlockPos);
		starts.forEach(v -> VecHelper.write(v, buffer));
		axes.forEach(v -> VecHelper.write(v, buffer));
		normals.forEach(v -> VecHelper.write(v, buffer));
		trackEnds.forEach(buffer::writeBoolean);
		buffer.writeBoolean(primary);
	}

	public BlockPos getKey() {
		return tePositions.getSecond();
	}

	public boolean isPrimary() {
		return primary;
	}

	// Runtime information

	public double getLength() {
		resolve();
		return length;
	}

	public float[] getStepLUT() {
		resolve();
		return stepLUT;
	}

	public int getSegmentCount() {
		resolve();
		return segments;
	}

	public Vec3 getPosition(double t) {
		resolve();
		return VecHelper.bezier(starts.getFirst(), starts.getSecond(), finish1, finish2, (float) t);
	}

	public double getRadius() {
		resolve();
		return radius;
	}

	public double getHandleLength() {
		resolve();
		return handleLength;
	}

	public double incrementT(double currentT, double distance) {
		resolve();
		double dx =
			VecHelper.bezierDerivative(starts.getFirst(), starts.getSecond(), finish1, finish2, (float) currentT)
				.length() / getLength();
		return currentT + distance / dx;

	}

	public Vec3 getNormal(double t) {
		resolve();
		Vec3 end1 = starts.getFirst();
		Vec3 end2 = starts.getSecond();
		Vec3 fn1 = normals.getFirst();
		Vec3 fn2 = normals.getSecond();

		Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, (float) t)
			.normalize();
		Vec3 faceNormal = fn1.equals(fn2) ? fn1 : VecHelper.slerp((float) t, fn1, fn2);
		Vec3 normal = faceNormal.cross(derivative)
			.normalize();
		return derivative.cross(normal);
	}

	private void resolve() {
		if (resolved)
			return;
		resolved = true;

		Vec3 end1 = starts.getFirst();
		Vec3 end2 = starts.getSecond();
		Vec3 axis1 = axes.getFirst()
			.normalize();
		Vec3 axis2 = axes.getSecond()
			.normalize();

		determineHandles(end1, end2, axis1, axis2);

		finish1 = axis1.scale(handleLength)
			.add(end1);
		finish2 = axis2.scale(handleLength)
			.add(end2);

		int scanCount = 16;
		length = 0;

		{
			Vec3 previous = end1;
			for (int i = 0; i <= scanCount; i++) {
				float t = i / (float) scanCount;
				Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
				if (previous != null)
					length += result.distanceTo(previous);
				previous = result;
			}
		}

		segments = (int) (length * 2);
		stepLUT = new float[segments + 1];
		stepLUT[0] = 1;
		float combinedDistance = 0;

		// determine step lut
		{
			Vec3 previous = end1;
			for (int i = 0; i <= segments; i++) {
				float t = i / (float) segments;
				Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
				if (i > 0) {
					combinedDistance += result.distanceTo(previous) / length;
					stepLUT[i] = (float) (t / combinedDistance);
				}
				previous = result;
			}
		}
	}

	private void determineHandles(Vec3 end1, Vec3 end2, Vec3 axis1, Vec3 axis2) {
		Vec3 cross1 = axis1.cross(new Vec3(0, 1, 0));
		Vec3 cross2 = axis2.cross(new Vec3(0, 1, 0));

		radius = 0;
		double a1 = Mth.atan2(-axis2.z, -axis2.x);
		double a2 = Mth.atan2(axis1.z, axis1.x);
		double angle = a1 - a2;

		float circle = 2 * Mth.PI;
		angle = (angle + circle) % circle;
		if (Math.abs(circle - angle) < Math.abs(angle))
			angle = circle - angle;

		if (Mth.equal(angle, 0)) {
			double[] intersect = VecHelper.intersect(end1, end2, axis1, cross2, Axis.Y);
			if (intersect != null) {
				double t = Math.abs(intersect[0]);
				double u = Math.abs(intersect[1]);
				double min = Math.min(t, u);
				double max = Math.max(t, u);

				if (min > 1.2 && max / min > 1 && max / min < 3) {
					handleLength = (max - min);
					return;
				}
			}

			handleLength = end2.distanceTo(end1) / 3;
			return;
		}

		double n = circle / angle;
		double factor = 4 / 3d * Math.tan(Math.PI / (2 * n));
		double[] intersect = VecHelper.intersect(end1, end2, cross1, cross2, Axis.Y);

		if (intersect == null) {
			handleLength = end2.distanceTo(end1) / 3;
			return;
		}

		radius = Math.abs(intersect[1]);
		handleLength = radius * factor;
		if (Mth.equal(handleLength, 0))
			handleLength = 1;
	}

}