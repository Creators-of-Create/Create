package com.simibubi.create.foundation.collision;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.world.phys.Vec3;

public class ContinuousOBBCollider extends OBBCollider {

	public static ContinuousSeparationManifold separateBBs(Vec3 cA, Vec3 cB, Vec3 eA, Vec3 eB,
		Matrix3d m, Vec3 motion) {
		ContinuousSeparationManifold mf = new ContinuousSeparationManifold();

		Vec3 diff = cB.subtract(cA);

		m.transpose();
		Vec3 diff2 = m.transform(diff);
		Vec3 motion2 = m.transform(motion);
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

		Vec3 uB0 = new Vec3(m.m00, m.m10, m.m20);
		Vec3 uB1 = new Vec3(m.m01, m.m11, m.m21);
		Vec3 uB2 = new Vec3(m.m02, m.m12, m.m22);

		checkCount = 0;
		mf.stepSeparationAxis = uB1;
		mf.stepSeparation = Double.MAX_VALUE;
		mf.normalSeparation = Double.MAX_VALUE;

		if (
		// Separate along A's local axes (global XYZ)
		!(separate(mf, uA0, diff.x, eA.x, a00 * eB.x + a01 * eB.y + a02 * eB.z, motion.x, true)
			|| separate(mf, uA1, diff.y, eA.y, a10 * eB.x + a11 * eB.y + a12 * eB.z, motion.y, true)
			|| separate(mf, uA2, diff.z, eA.z, a20 * eB.x + a21 * eB.y + a22 * eB.z, motion.z, true)

			// Separate along B's local axes
			|| separate(mf, uB0, diff2.x, eA.x * a00 + eA.y * a10 + eA.z * a20, eB.x, motion2.x, false)
			|| separate(mf, uB1, diff2.y, eA.x * a01 + eA.y * a11 + eA.z * a21, eB.y, motion2.y, false)
			|| separate(mf, uB2, diff2.z, eA.x * a02 + eA.y * a12 + eA.z * a22, eB.z, motion2.z, false)))
			return mf;

		return null;
	}

	static boolean separate(ContinuousSeparationManifold mf, Vec3 axis, double TL, double rA, double rB,
		double projectedMotion, boolean axisOfObjA) {
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

		Vec3 normalizedAxis = axis.normalize();

		boolean isBestSeperation = distance != 0 && -(diff) <= abs(mf.separation);
		// boolean isBestSeperation = discreteCollision && checkCount == 5; // Debug specific separations

		if (axisOfObjA && distance != 0 && -(diff) <= abs(mf.normalSeparation)) {
			mf.normalAxis = normalizedAxis;
			mf.normalSeparation = seperation;
		}

		double dot = mf.stepSeparationAxis.dot(axis);
		if (dot != 0 && discreteCollision) {
			Vec3 cross = axis.cross(mf.stepSeparationAxis);
			double dotSeparation = signum(dot) * TL - (rA + rB);
			double stepSeparation = -dotSeparation;
			Vec3 stepSeparationVec = axis;

			if (!cross.equals(Vec3.ZERO)) {
				Vec3 sepVec = normalizedAxis.scale(dotSeparation);
				Vec3 axisPlane = axis.cross(cross);
				Vec3 stepPlane = mf.stepSeparationAxis.cross(cross);
				stepSeparationVec =
					sepVec.subtract(axisPlane.scale(sepVec.dot(stepPlane) / axisPlane.dot(stepPlane)));
				stepSeparation = stepSeparationVec.length();
				if (abs(mf.stepSeparation) > abs(stepSeparation) && stepSeparation != 0)
					mf.stepSeparation = stepSeparation;

			} else {
				if (abs(mf.stepSeparation) > stepSeparation)
					mf.stepSeparation = stepSeparation;
			}
		}

		if (isBestSeperation) {
			mf.axis = normalizedAxis;
			mf.separation = seperation;
			mf.collisionPosition =
				normalizedAxis.scale(signum(TL) * (axisOfObjA ? -rB : -rB) - signum(seperation) * .125f);
		}

		return false;
	}

	public static class ContinuousSeparationManifold extends SeparationManifold {

		static final double UNDEFINED = -1;
		double latestCollisionEntryTime = UNDEFINED;
		double earliestCollisionExitTime = Double.MAX_VALUE;
		boolean isDiscreteCollision = true;
		Vec3 collisionPosition;

		Vec3 stepSeparationAxis;
		double stepSeparation;

		Vec3 normalAxis;
		double normalSeparation;

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

		public Vec3 getCollisionNormal() {
			return normalAxis == null ? null : createSeparationVec(normalSeparation, normalAxis);
		}

		public Vec3 getCollisionPosition() {
			return collisionPosition;
		}

		public Vec3 asSeparationVec(double obbStepHeight) {
			if (isDiscreteCollision) {
				if (stepSeparation <= obbStepHeight)
					return createSeparationVec(stepSeparation, stepSeparationAxis);
				return super.asSeparationVec();
			}
			double t = getTimeOfImpact();
			if (t == UNDEFINED)
				return null;
			return Vec3.ZERO;
		}

		@Override
		public Vec3 asSeparationVec() {
			return asSeparationVec(0);
		}

	}

}
