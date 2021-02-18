package com.simibubi.create.content.schematics.client;

import static java.lang.Math.abs;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingAngle;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.PlacementSettings;

public class SchematicTransformation {

	private InterpolatedChasingValue x, y, z, scaleFrontBack, scaleLeftRight;
	private InterpolatedChasingAngle rotation;
	private double xOrigin;
	private double zOrigin;

	public SchematicTransformation() {
		x = new InterpolatedChasingValue();
		y = new InterpolatedChasingValue();
		z = new InterpolatedChasingValue();
		scaleFrontBack = new InterpolatedChasingValue();
		scaleLeftRight = new InterpolatedChasingValue();
		rotation = new InterpolatedChasingAngle();
	}

	public void init(BlockPos anchor, PlacementSettings settings, AxisAlignedBB bounds) {
		int leftRight = settings.getMirror() == Mirror.LEFT_RIGHT ? -1 : 1;
		int frontBack = settings.getMirror() == Mirror.FRONT_BACK ? -1 : 1;
		getScaleFB().start(frontBack);
		getScaleLR().start(leftRight);
		xOrigin = bounds.getXSize() / 2f;
		zOrigin = bounds.getZSize() / 2f;

		int r = -(settings.getRotation()
			.ordinal() * 90);
		rotation.start(r);

		Vector3d vec = fromAnchor(anchor);
		x.start((float) vec.x);
		y.start((float) vec.y);
		z.start((float) vec.z);
	}

	public void applyGLTransformations(MatrixStack ms) {
		float pt = AnimationTickHolder.getPartialTicks();

		// Translation
		ms.translate(x.get(pt), y.get(pt), z.get(pt));
		Vector3d rotationOffset = getRotationOffset(true);

		// Rotation & Mirror
		float fb = getScaleFB().get(pt);
		float lr = getScaleLR().get(pt);
		float rot = rotation.get(pt) + ((fb < 0 && lr < 0) ? 180 : 0);
		ms.translate(xOrigin, 0, zOrigin);
		MatrixStacker.of(ms)
			.translate(rotationOffset)
			.rotateY(rot)
			.translateBack(rotationOffset);
		ms.scale(abs(fb), 1, abs(lr));
		ms.translate(-xOrigin, 0, -zOrigin);

	}

	public boolean isFlipped() {
		return getMirrorModifier(Axis.X) < 0 != getMirrorModifier(Axis.Z) < 0;
	}

	public Vector3d getRotationOffset(boolean ignoreMirrors) {
		Vector3d rotationOffset = Vector3d.ZERO;
		if ((int) (zOrigin * 2) % 2 != (int) (xOrigin * 2) % 2) {
			boolean xGreaterZ = xOrigin > zOrigin;
			float xIn = (xGreaterZ ? 0 : .5f);
			float zIn = (!xGreaterZ ? 0 : .5f);
			if (!ignoreMirrors) {
				xIn *= getMirrorModifier(Axis.X);
				zIn *= getMirrorModifier(Axis.Z);
			}
			rotationOffset = new Vector3d(xIn, 0, zIn);
		}
		return rotationOffset;
	}

	public Vector3d toLocalSpace(Vector3d vec) {
		float pt = AnimationTickHolder.getPartialTicks();
		Vector3d rotationOffset = getRotationOffset(true);

		vec = vec.subtract(x.get(pt), y.get(pt), z.get(pt));
		vec = vec.subtract(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
		vec = VecHelper.rotate(vec, -rotation.get(pt), Axis.Y);
		vec = vec.add(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(getScaleFB().get(pt), 1, getScaleLR().get(pt));
		vec = vec.add(xOrigin, 0, zOrigin);

		return vec;
	}

	public PlacementSettings toSettings() {
		PlacementSettings settings = new PlacementSettings();

		int i = (int) rotation.getTarget();

		boolean mirrorlr = getScaleLR().getTarget() < 0;
		boolean mirrorfb = getScaleFB().getTarget() < 0;
		if (mirrorlr && mirrorfb) {
			mirrorlr = mirrorfb = false;
			i += 180;
		}
		i = i % 360;
		if (i < 0)
			i += 360;

		Rotation rotation = Rotation.NONE;
		switch (i) {
		case 90:
			rotation = Rotation.COUNTERCLOCKWISE_90;
			break;
		case 180:
			rotation = Rotation.CLOCKWISE_180;
			break;
		case 270:
			rotation = Rotation.CLOCKWISE_90;
			break;
		default:
		}

		settings.setRotation(rotation);
		if (mirrorfb)
			settings.setMirror(Mirror.FRONT_BACK);
		if (mirrorlr)
			settings.setMirror(Mirror.LEFT_RIGHT);

		return settings;
	}

	public BlockPos getAnchor() {
		Vector3d vec = Vector3d.ZERO.add(.5, 0, .5);
		Vector3d rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(getScaleFB().getTarget(), 1, getScaleLR().getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);

		vec = vec.add(x.getTarget(), y.getTarget(), z.getTarget());
		return new BlockPos(vec.x, vec.y, vec.z);
	}

	public Vector3d fromAnchor(BlockPos pos) {
		Vector3d vec = Vector3d.ZERO.add(.5, 0, .5);
		Vector3d rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(getScaleFB().getTarget(), 1, getScaleLR().getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);

		return Vector3d.of(pos.subtract(new BlockPos(vec.x, vec.y, vec.z)));
	}

	public int getRotationTarget() {
		return (int) rotation.getTarget();
	}

	public int getMirrorModifier(Axis axis) {
		if (axis == Axis.Z)
			return (int) getScaleLR().getTarget();
		return (int) getScaleFB().getTarget();
	}

	public float getCurrentRotation() {
		float pt = AnimationTickHolder.getPartialTicks();
		return rotation.get(pt);
	}

	public void tick() {
		x.tick();
		y.tick();
		z.tick();
		getScaleLR().tick();
		getScaleFB().tick();
		rotation.tick();
	}

	public void flip(Axis axis) {
		if (axis == Axis.X)
			getScaleLR().target(getScaleLR().getTarget() * -1);
		if (axis == Axis.Z)
			getScaleFB().target(getScaleFB().getTarget() * -1);
	}

	public void rotate90(boolean clockwise) {
		rotation.target(rotation.getTarget() + (clockwise ? -90 : 90));
	}

	public void move(float xIn, float yIn, float zIn) {
		moveTo(x.getTarget() + xIn, y.getTarget() + yIn, z.getTarget() + zIn);
	}

	public void startAt(BlockPos pos) {
		x.start(pos.getX());
		y.start(0);
		z.start(pos.getZ());
		moveTo(pos);
	}
	
	public void moveTo(BlockPos pos) {
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}

	public void moveTo(float xIn, float yIn, float zIn) {
		x.target(xIn);
		y.target(yIn);
		z.target(zIn);
	}

	public InterpolatedChasingValue getScaleFB() {
		return scaleFrontBack;
	}

	public InterpolatedChasingValue getScaleLR() {
		return scaleLeftRight;
	}

}
