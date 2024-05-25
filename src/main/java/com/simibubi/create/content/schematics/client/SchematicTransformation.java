package com.simibubi.create.content.schematics.client;

import static java.lang.Math.abs;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SchematicTransformation {

	private Vec3 chasingPos;
	private Vec3 prevChasingPos;
	private BlockPos target;

	private LerpedFloat scaleFrontBack, scaleLeftRight;
	private LerpedFloat rotation;
	private double xOrigin;
	private double zOrigin;

	public SchematicTransformation() {
		chasingPos = Vec3.ZERO;
		prevChasingPos = Vec3.ZERO;
		target = BlockPos.ZERO;
		scaleFrontBack = LerpedFloat.linear();
		scaleLeftRight = LerpedFloat.linear();
		rotation = LerpedFloat.angular();
	}

	public void init(BlockPos anchor, StructurePlaceSettings settings, AABB bounds) {
		int leftRight = settings.getMirror() == Mirror.LEFT_RIGHT ? -1 : 1;
		int frontBack = settings.getMirror() == Mirror.FRONT_BACK ? -1 : 1;
		getScaleFB().chase(0, 0.45f, Chaser.EXP)
			.startWithValue(frontBack);
		getScaleLR().chase(0, 0.45f, Chaser.EXP)
			.startWithValue(leftRight);
		xOrigin = bounds.getXsize() / 2f;
		zOrigin = bounds.getZsize() / 2f;

		int r = -(settings.getRotation()
			.ordinal() * 90);
		rotation.chase(0, 0.45f, Chaser.EXP)
			.startWithValue(r);

		target = fromAnchor(anchor);
		chasingPos = Vec3.atLowerCornerOf(target);
		prevChasingPos = chasingPos;
	}

	public void applyTransformations(PoseStack ms, Vec3 camera) {
		float pt = AnimationTickHolder.getPartialTicks();

		// Translation
		TransformStack.of(ms)
			.translate(VecHelper.lerp(pt, prevChasingPos, chasingPos)
				.subtract(camera));
		Vec3 rotationOffset = getRotationOffset(true);

		// Rotation & Mirror
		float fb = getScaleFB().getValue(pt);
		float lr = getScaleLR().getValue(pt);
		float rot = rotation.getValue(pt) + ((fb < 0 && lr < 0) ? 180 : 0);
		ms.translate(xOrigin, 0, zOrigin);
		TransformStack.of(ms)
			.translate(rotationOffset)
			.rotateYDegrees(rot)
			.translateBack(rotationOffset);
		ms.scale(abs(fb), 1, abs(lr));
		ms.translate(-xOrigin, 0, -zOrigin);

	}

	public boolean isFlipped() {
		return getMirrorModifier(Axis.X) < 0 != getMirrorModifier(Axis.Z) < 0;
	}

	public Vec3 getRotationOffset(boolean ignoreMirrors) {
		Vec3 rotationOffset = Vec3.ZERO;
		if ((int) (zOrigin * 2) % 2 != (int) (xOrigin * 2) % 2) {
			boolean xGreaterZ = xOrigin > zOrigin;
			float xIn = (xGreaterZ ? 0 : .5f);
			float zIn = (!xGreaterZ ? 0 : .5f);
			if (!ignoreMirrors) {
				xIn *= getMirrorModifier(Axis.X);
				zIn *= getMirrorModifier(Axis.Z);
			}
			rotationOffset = new Vec3(xIn, 0, zIn);
		}
		return rotationOffset;
	}

	public Vec3 toLocalSpace(Vec3 vec) {
		float pt = AnimationTickHolder.getPartialTicks();
		Vec3 rotationOffset = getRotationOffset(true);

		vec = vec.subtract(VecHelper.lerp(pt, prevChasingPos, chasingPos));
		vec = vec.subtract(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
		vec = VecHelper.rotate(vec, -rotation.getValue(pt), Axis.Y);
		vec = vec.add(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.multiply(getScaleFB().getValue(pt), 1, getScaleLR().getValue(pt));
		vec = vec.add(xOrigin, 0, zOrigin);

		return vec;
	}

	public StructurePlaceSettings toSettings() {
		StructurePlaceSettings settings = new StructurePlaceSettings();

		int i = (int) rotation.getChaseTarget();

		boolean mirrorlr = getScaleLR().getChaseTarget() < 0;
		boolean mirrorfb = getScaleFB().getChaseTarget() < 0;
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
		Vec3 vec = Vec3.ZERO.add(.5, 0, .5);
		Vec3 rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.multiply(getScaleFB().getChaseTarget(), 1, getScaleLR().getChaseTarget());
		vec = VecHelper.rotate(vec, rotation.getChaseTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);
		vec = vec.add(target.getX(), target.getY(), target.getZ());
		return BlockPos.containing(vec.x, vec.y, vec.z);
	}

	public BlockPos fromAnchor(BlockPos pos) {
		Vec3 vec = Vec3.ZERO.add(.5, 0, .5);
		Vec3 rotationOffset = getRotationOffset(false);
		vec = vec.subtract(xOrigin, 0, zOrigin);
		vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
		vec = vec.multiply(getScaleFB().getChaseTarget(), 1, getScaleLR().getChaseTarget());
		vec = VecHelper.rotate(vec, rotation.getChaseTarget(), Axis.Y);
		vec = vec.add(xOrigin, 0, zOrigin);
		return pos.subtract(BlockPos.containing(vec.x, vec.y, vec.z));
	}

	public int getRotationTarget() {
		return (int) rotation.getChaseTarget();
	}

	public int getMirrorModifier(Axis axis) {
		if (axis == Axis.Z)
			return (int) getScaleLR().getChaseTarget();
		return (int) getScaleFB().getChaseTarget();
	}

	public float getCurrentRotation() {
		float pt = AnimationTickHolder.getPartialTicks();
		return rotation.getValue(pt);
	}

	public void tick() {
		prevChasingPos = chasingPos;
		chasingPos = VecHelper.lerp(0.45f, chasingPos, Vec3.atLowerCornerOf(target));
		getScaleLR().tickChaser();
		getScaleFB().tickChaser();
		rotation.tickChaser();
	}

	public void flip(Axis axis) {
		if (axis == Axis.X)
			getScaleLR().updateChaseTarget(getScaleLR().getChaseTarget() * -1);
		if (axis == Axis.Z)
			getScaleFB().updateChaseTarget(getScaleFB().getChaseTarget() * -1);
	}

	public void rotate90(boolean clockwise) {
		rotation.updateChaseTarget(rotation.getChaseTarget() + (clockwise ? -90 : 90));
	}

	public void move(int xIn, int yIn, int zIn) {
		moveTo(target.offset(xIn, yIn, zIn));
	}

	public void startAt(BlockPos pos) {
		chasingPos = Vec3.atLowerCornerOf(pos);
		prevChasingPos = chasingPos;
		moveTo(pos);
	}

	public void moveTo(BlockPos pos) {
		target = pos;
	}

	public void moveTo(int xIn, int yIn, int zIn) {
		moveTo(new BlockPos(xIn, yIn, zIn));
	}

	public LerpedFloat getScaleFB() {
		return scaleFrontBack;
	}

	public LerpedFloat getScaleLR() {
		return scaleLeftRight;
	}

}
