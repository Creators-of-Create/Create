package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ArmAngleTarget {

	static final ArmAngleTarget NO_TARGET = new ArmAngleTarget();

	float baseAngle;
	float lowerArmAngle;
	float upperArmAngle;
	float headAngle;

	private ArmAngleTarget() {
		lowerArmAngle = 135;
		upperArmAngle = 45;
		headAngle = 0;
	}

	public ArmAngleTarget(BlockPos armPos, Vec3 pointTarget, Direction clawFacing, boolean ceiling) {
		Vec3 target = pointTarget;
		Vec3 origin = VecHelper.getCenterOf(armPos)
			.add(0, ceiling ? -6 / 16f : 6 / 16f, 0);
		Vec3 clawTarget = target;
		target = target.add(Vec3.atLowerCornerOf(clawFacing.getOpposite()
			.getNormal())
			.scale(.5f));

		Vec3 diff = target.subtract(origin);
		float horizontalDistance = (float) diff.multiply(1, 0, 1)
			.length();

		float baseAngle = AngleHelper.deg(Mth.atan2(diff.x, diff.z)) + 180;
		if (ceiling) {
			diff = diff.multiply(1, -1, 1);
			baseAngle = 180 - baseAngle;
		}

		float alphaOffset = AngleHelper.deg(Mth.atan2(diff.y, horizontalDistance));

		float a = 14 / 16f; // lower arm length
		float a2 = a * a;
		float b = 15 / 16f; // upper arm length
		float b2 = b * b;
		float diffLength =
			Mth.clamp(Mth.sqrt((float) (diff.y * diff.y + horizontalDistance * horizontalDistance)), 1 / 8f, a + b);
		float diffLength2 = diffLength * diffLength;

		float alphaRatio = (-b2 + a2 + diffLength2) / (2 * a * diffLength);
		float alpha = AngleHelper.deg(Math.acos(alphaRatio)) + alphaOffset;
		float betaRatio = (-diffLength2 + a2 + b2) / (2 * b * a);
		float beta = AngleHelper.deg(Math.acos(betaRatio));

		if (Float.isNaN(alpha))
			alpha = 0;
		if (Float.isNaN(beta))
			beta = 0;

		Vec3 headPos = new Vec3(0, 0, 0);
		headPos = VecHelper.rotate(headPos.add(0, b, 0), beta + 180, Axis.X);
		headPos = VecHelper.rotate(headPos.add(0, a, 0), alpha - 90, Axis.X);
		headPos = VecHelper.rotate(headPos, baseAngle, Axis.Y);
		headPos = VecHelper.rotate(headPos, ceiling ? 180 : 0, Axis.X);
		headPos = headPos.add(origin);
		Vec3 headDiff = clawTarget.subtract(headPos);

		if (ceiling)
			headDiff = headDiff.multiply(1, -1, 1);

		float horizontalHeadDistance = (float) headDiff.multiply(1, 0, 1)
			.length();
		float headAngle = (float) (alpha + beta + 135 - AngleHelper.deg(Mth.atan2(headDiff.y, horizontalHeadDistance)));

		this.lowerArmAngle = alpha;
		this.upperArmAngle = beta;
		this.headAngle = -headAngle;
		this.baseAngle = baseAngle;
	}

}
