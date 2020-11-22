package com.simibubi.create.foundation.collision;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.util.math.Vec3d;

public class ContinuousOBBCollider extends OBBCollider {

	public static ContinuousSeparationManifold separateBBs(Vec3d cA, Vec3d cB, Vec3d eA, Vec3d eB, Matrix3d m,
		Vec3d motion) {
		ContinuousSeparationManifold mf = new ContinuousSeparationManifold();

		Vec3d diff = cB.subtract(cA);

		m.transpose();
		Vec3d diff2 = m.transform(diff);
		Vec3d motion2 = m.transform(motion);
		m.transpose();

		double a00 = abs(m.m00);
		double a01 = abs(m.m01);
		double a02 = abs(m.m02);
		double a10 = abs(m.m10);
		double a11 = abs(m.m11);
		double a12 = abs(m.m12);
		double a20 = abs(m.m20);
		double a21 = abs(m.m21);
		double a22 = abs(m.m22);

		Vec3d uB0 = new Vec3d(m.m00, m.m10, m.m20);
		Vec3d uB1 = new Vec3d(m.m01, m.m11, m.m21);
		Vec3d uB2 = new Vec3d(m.m02, m.m12, m.m22);

		checkCount = 0;
		mf.stepSeparationAxis = uB1;
		mf.stepSeparation = Double.MAX_VALUE;

		if (
		// Separate along A's local axes (global XYZ)
		!(separate(mf, uA0, diff.x, eA.x, a00 * eB.x + a01 * eB.y + a02 * eB.z, motion.x)
			|| separate(mf, uA1, diff.y, eA.y, a10 * eB.x + a11 * eB.y + a12 * eB.z, motion.y)
			|| separate(mf, uA2, diff.z, eA.z, a20 * eB.x + a21 * eB.y + a22 * eB.z, motion.z)

			// Separate along B's local axes
			|| separate(mf, uB0, diff2.x, eA.x * a00 + eA.y * a10 + eA.z * a20, eB.x, motion2.x)
			|| separate(mf, uB1, diff2.y, eA.x * a01 + eA.y * a11 + eA.z * a21, eB.y, motion2.y)
			|| separate(mf, uB2, diff2.z, eA.x * a02 + eA.y * a12 + eA.z * a22, eB.z, motion2.z)))
			return mf;

		return null;
	}

	static boolean separate(ContinuousSeparationManifold mf, Vec3d axis, double TL, double rA, double rB,
		double projectedMotion) {
		checkCount++;
		double distance = abs(TL);
		double diff = distance - (rA + rB);

		boolean discreteCollision = diff <= 0;
		if (!discreteCollision && signum(projectedMotion) == signum(TL))
			return true;

		double sTL = signum(TL);
		double seperation = sTL * abs(diff);

		double entryTime = 0;
		double exitTime = Double.MAX_VALUE;
		if (!discreteCollision) {
			mf.isDiscreteCollision = false;

			if (abs(seperation) > abs(projectedMotion))
				return true;

			entryTime = abs(seperation) / abs(projectedMotion);
			exitTime = (diff + abs(rA) + abs(rB)) / abs(projectedMotion);
			mf.latestCollisionEntryTime = Math.max(entryTime, mf.latestCollisionEntryTime);
			mf.earliestCollisionExitTime = Math.min(exitTime, mf.earliestCollisionExitTime);
		}

		Vec3d normalizedAxis = axis.normalize();

		boolean isBestSeperation = distance != 0 && -(diff) <= abs(mf.separation);
//		boolean isBestSeperation = discreteCollision && checkCount == 5; // Debug specific separations

		double dot = mf.stepSeparationAxis.dotProduct(axis);
		if (dot != 0 && discreteCollision) {
			Vec3d cross = axis.crossProduct(mf.stepSeparationAxis);
			double dotSeparation = signum(dot) * TL - (rA + rB);
			double stepSeparation = -dotSeparation;
			Vec3d stepSeparationVec = axis;

			if (!cross.equals(Vec3d.ZERO)) {
				Vec3d sepVec = normalizedAxis.scale(dotSeparation);
				Vec3d axisPlane = axis.crossProduct(cross);
				Vec3d stepPlane = mf.stepSeparationAxis.crossProduct(cross);
				stepSeparationVec =
					sepVec.subtract(axisPlane.scale(sepVec.dotProduct(stepPlane) / axisPlane.dotProduct(stepPlane)));
				stepSeparation = stepSeparationVec.length();


				if (abs(mf.stepSeparation) > abs(stepSeparation) && stepSeparation != 0) {
//					CollisionDebugger.showDebugLine(Vec3d.ZERO, sepVec, 0x111155, "stepsep", -16);
					mf.stepSeparation = stepSeparation;
				}

			} else {
				if (abs(mf.stepSeparation) > stepSeparation) {
					mf.stepSeparation = stepSeparation;
//					CollisionDebugger.showDebugLine(Vec3d.ZERO, stepSeparationVec, 0xff9999, "axis", -16);
				}
			}

//			if (abs(mf.separation) < abs(stepSeparation) && stepSeparation != 0)
		}

		if (isBestSeperation) {

			mf.axis = normalizedAxis;
			mf.separation = seperation;

			// Visualize values
//			if (CollisionDebugger.AABB != null) {
//				Vec3d normalizedAxis = axis.normalize();
//				showDebugLine(Vec3d.ZERO, normalizedAxis.scale(projectedMotion), 0x111155, "motion", 5);
//				showDebugLine(Vec3d.ZERO, normalizedAxis.scale(TL), 0xbb00bb, "tl", 4);
//				showDebugLine(Vec3d.ZERO, normalizedAxis.scale(sTL * rA), 0xff4444, "ra", 3);
//				showDebugLine(normalizedAxis.scale(sTL * rA),
//					normalizedAxis.scale(sTL * rA - entryTime * projectedMotion), 0x44ff44, "entry", 0);
//				showDebugLine(normalizedAxis.scale(sTL * rA - entryTime * projectedMotion),
//					normalizedAxis.scale(sTL * rA - entryTime * projectedMotion + exitTime * projectedMotion), 0x44ffff,
//					"exit", -1);
//				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)), normalizedAxis.scale(TL), 0x4444ff, "rb", 2);
//				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)),
//					normalizedAxis.scale(sTL * (distance - rB) + value), 0xff9966, "separation", 1);
////				System.out.println("TL:" + TL + ", rA: " + rA + ", rB: " + rB);
//			}

		}

		return false;
	}

	public static class ContinuousSeparationManifold extends SeparationManifold {

		static final double UNDEFINED = -1;
		double latestCollisionEntryTime = UNDEFINED;
		double earliestCollisionExitTime = Double.MAX_VALUE;
		boolean isDiscreteCollision = true;

		Vec3d stepSeparationAxis;
		double stepSeparation;

		public double getTimeOfImpact() {
			if (latestCollisionEntryTime == UNDEFINED)
				return UNDEFINED;
			if (latestCollisionEntryTime > earliestCollisionExitTime)
				return UNDEFINED;
			return latestCollisionEntryTime;
		}

		public boolean isSurfaceCollision() {
			return true;
		}

		public Vec3d asSeparationVec(double obbStepHeight) {
			if (isDiscreteCollision) {
				if (stepSeparation <= obbStepHeight) 
					return createSeparationVec(stepSeparation, stepSeparationAxis);
				return super.asSeparationVec();
			}
			double t = getTimeOfImpact();
			if (t == UNDEFINED)
				return null;
			return Vec3d.ZERO;
		}
		
		@Override
		public Vec3d asSeparationVec() {
			return asSeparationVec(0);
		}

	}

}
