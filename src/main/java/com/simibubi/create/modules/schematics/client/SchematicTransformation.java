package com.simibubi.create.modules.schematics.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingAngle;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
		scaleFrontBack.start(frontBack);
		scaleLeftRight.start(leftRight);
		xOrigin = bounds.getXSize() / 2f;
		zOrigin = bounds.getZSize() / 2f;

		int r = -(settings.getRotation().ordinal() * 90);
		rotation.start(r);

		Vec3d vec = fromAnchor(anchor);
		x.start((float) vec.x);
		y.start((float) vec.y);
		z.start((float) vec.z);
	}

	public void applyGLTransformations() {
		float pt = Minecraft.getInstance().getRenderPartialTicks();

		// Translation
		GlStateManager.translated(x.get(pt), y.get(pt), z.get(pt));

		Vec3d rotationOffset = getRotationOffset(true);

		// Rotation & Mirror
		GlStateManager.translated(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
		GlStateManager.rotated(rotation.get(pt), 0, 1, 0);
		GlStateManager.translated(-rotationOffset.x, 0, -rotationOffset.z);
		GlStateManager.scaled(scaleFrontBack.get(pt), 1, scaleLeftRight.get(pt));
		GlStateManager.translated(-xOrigin, 0, -zOrigin);

	}

	public Vec3d getRotationOffset(boolean ignoreMirrors) {
		Vec3d rotationOffset = Vec3d.ZERO;
		if ((int) (zOrigin * 2) % 2 != (int) (xOrigin * 2) % 2) {
			boolean xGreaterZ = xOrigin > zOrigin;
			float xIn = (xGreaterZ ? 0 : .5f);
			float zIn = (!xGreaterZ ? 0 : .5f);
			if (!ignoreMirrors) {
				xIn *= getMirrorModifier(Axis.X);
				zIn *= getMirrorModifier(Axis.Z);
			}
			rotationOffset = new Vec3d(xIn, 0, zIn);
		}
		return rotationOffset;
	}

	public Vec3d toLocalSpace(Vec3d vec) {
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		Vec3d rotationOffset = getRotationOffset(true);

		vec = vec.subtract(x.get(pt), y.get(pt), z.get(pt));
		vec = vec.subtract(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
		vec = VecHelper.rotate(vec, -rotation.get(pt), Axis.Y);
		vec = vec.add(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(scaleFrontBack.get(pt), 1, scaleLeftRight.get(pt));
		vec = vec.add(xOrigin, 0, zOrigin);

		return vec;
	}

	public PlacementSettings toSettings() {
		PlacementSettings settings = new PlacementSettings();

		int i = (int) rotation.getTarget();

		boolean mirrorlr = scaleLeftRight.getTarget() < 0;
		boolean mirrorfb = scaleFrontBack.getTarget() < 0;
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
		Vec3d vec = Vec3d.ZERO.add(.5, 0, .5);
		Vec3d rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(scaleFrontBack.getTarget(), 1, scaleLeftRight.getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);

		vec = vec.add(x.getTarget(), y.getTarget(), z.getTarget());
		return new BlockPos(vec.x, vec.y, vec.z);
	}

	public Vec3d fromAnchor(BlockPos pos) {
		Vec3d vec = Vec3d.ZERO.add(.5, 0, .5);
		Vec3d rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.mul(scaleFrontBack.getTarget(), 1, scaleLeftRight.getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);

		return new Vec3d(pos.subtract(new BlockPos(vec.x, vec.y, vec.z)));
	}

	public int getRotationTarget() {
		return (int) rotation.getTarget();
	}

	public int getMirrorModifier(Axis axis) {
		if (axis == Axis.Z)
			return (int) scaleLeftRight.getTarget();
		return (int) scaleFrontBack.getTarget();
	}

	public float getCurrentRotation() {
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		return rotation.get(pt);
	}

	public void tick() {
		x.tick();
		y.tick();
		z.tick();
		scaleLeftRight.tick();
		scaleFrontBack.tick();
		rotation.tick();
	}

	public void flip(Axis axis) {
		if (axis == Axis.X)
			scaleLeftRight.target(scaleLeftRight.getTarget() * -1);
		if (axis == Axis.Z)
			scaleFrontBack.target(scaleFrontBack.getTarget() * -1);
	}

	public void rotate90(boolean clockwise) {
		rotation.target(rotation.getTarget() + (clockwise ? -90 : 90));
	}

	public void move(float xIn, float yIn, float zIn) {
		moveTo(x.getTarget() + xIn, y.getTarget() + yIn, z.getTarget() + zIn);
	}

	public void moveTo(BlockPos pos) {
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}

	public void moveTo(float xIn, float yIn, float zIn) {
		x.target(xIn);
		y.target(yIn);
		z.target(zIn);
	}

}
