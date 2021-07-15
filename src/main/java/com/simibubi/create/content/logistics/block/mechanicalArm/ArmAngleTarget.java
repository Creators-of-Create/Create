package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ArmAngleTarget {

	static ArmAngleTarget NO_TARGET = new ArmAngleTarget();

	float baseAngle;
	float lowerArmAngle;
	float upperArmAngle;
	float headAngle;

	private ArmAngleTarget() {
		lowerArmAngle = 155;
		upperArmAngle = 60;
		headAngle = -15;
	}

	public ArmAngleTarget(BlockPos armPos, Vector3d pointTarget, Direction clawFacing, boolean ceiling) {
//		if (ceiling) 
//			clawFacing = clawFacing.getOpposite();

		Vector3d target = pointTarget;
		Vector3d origin = VecHelper.getCenterOf(armPos)
			.add(0, ceiling ? -4 / 16f : 4 / 16f, 0);
		Vector3d clawTarget = target;
		target = target.add(Vector3d.atLowerCornerOf(clawFacing.getOpposite()
			.getNormal()).scale(.5f));

		Vector3d diff = target.subtract(origin);
		float horizontalDistance = (float) diff.multiply(1, 0, 1)
			.length();

		float baseAngle = AngleHelper.deg(MathHelper.atan2(diff.x, diff.z)) + 180;
		if (ceiling) {
			diff = diff.multiply(1, -1, 1);
			baseAngle = 180 - baseAngle;
		}

		float alphaOffset = AngleHelper.deg(MathHelper.atan2(diff.y, horizontalDistance));

		float a = 18 / 16f; // lower arm length
		float a2 = a * a;
		float b = 17 / 16f; // upper arm length
		float b2 = b * b;
		float diffLength =
			MathHelper.clamp(MathHelper.sqrt(diff.y * diff.y + horizontalDistance * horizontalDistance), 1 / 8f, a + b);
		float diffLength2 = diffLength * diffLength;

		float alphaRatio = (-b2 + a2 + diffLength2) / (2 * a * diffLength);
		float alpha = AngleHelper.deg(Math.acos(alphaRatio)) + alphaOffset;
		float betaRatio = (-diffLength2 + a2 + b2) / (2 * b * a);
		float beta = AngleHelper.deg(Math.acos(betaRatio));

		if (Float.isNaN(alpha))
			alpha = 0;
		if (Float.isNaN(beta))
			beta = 0;

		Vector3d headPos = new Vector3d(0, 0, 0);
		headPos = VecHelper.rotate(headPos.add(0, b, 0), beta + 180, Axis.X);
		headPos = VecHelper.rotate(headPos.add(0, a, 0), alpha - 90, Axis.X);
		headPos = VecHelper.rotate(headPos, baseAngle, Axis.Y);
		headPos = VecHelper.rotate(headPos, ceiling ? 180 : 0, Axis.X);
		headPos = headPos.add(origin);
		Vector3d headDiff = clawTarget.subtract(headPos);

		if (ceiling)
			headDiff = headDiff.multiply(1, -1, 1);

		float horizontalHeadDistance = (float) headDiff.multiply(1, 0, 1)
			.length();
		float headAngle =
			(float) (alpha + beta + 135 - AngleHelper.deg(MathHelper.atan2(headDiff.y, horizontalHeadDistance)));

		this.lowerArmAngle = alpha;
		this.upperArmAngle = beta;
		this.headAngle = -headAngle;
		this.baseAngle = baseAngle;
	}

}
