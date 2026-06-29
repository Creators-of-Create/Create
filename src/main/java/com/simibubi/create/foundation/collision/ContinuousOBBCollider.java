package com.simibubi.create.foundation.collision;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import com.simibubi.create.foundation.collision.CollisionList.Populate;

import net.createmod.catnip.data.Iterate;
import net.minecraft.world.phys.Vec3;

public class ContinuousOBBCollider {

	static final Vec3 uA0 = new Vec3(1, 0, 0);
	static final Vec3 uA1 = new Vec3(0, 1, 0);
	static final Vec3 uA2 = new Vec3(0, 0, 1);
	static int checkCount = 0;

	public static class CollisionResponse {
		public boolean surfaceCollision;
		public Vec3 collisionResponse;
		public Vec3 normal;
		public Vec3 location;
		public double temporalResponse;
	}

	public static CollisionResponse collideMany(CollisionList collidableBBs, CollisionList denseViableColliders, OrientedBB obb, Vec3 motion, float entityMaxStep, boolean doHorizontalPass) {
		Vec3 obbCenter = obb.center;
		Vec3 obbExtents = obb.extents;
		Matrix3d rotation = obb.rotation;

		double a00 = abs(rotation.m00);
		double a01 = abs(rotation.m01);
		double a02 = abs(rotation.m02);
		double a10 = abs(rotation.m10);
		double a11 = abs(rotation.m11);
		double a12 = abs(rotation.m12);
		double a20 = abs(rotation.m20);
		double a21 = abs(rotation.m21);
		double a22 = abs(rotation.m22);

		// The extents of the AABB encompassing the OBB, in local space.
		// https://madmann91.github.io/2024/02/10/converting-oriented-bounding-boxes-to-axis-aligned-ones.html
		double aabbInLocalX = (a00 + a01 + a02) * obbExtents.x + 0.5;
		double aabbInLocalY = (a10 + a11 + a12) * obbExtents.y + 0.5;
		double aabbInLocalZ = (a20 + a21 + a22) * obbExtents.z + 0.5;

		CollisionList.Populate populateDenseViableColliders = new Populate(denseViableColliders);

		// Filter down the list of potential colliders, accounting for the entity's motion.
		// We densify the set of colliders ahead of time to reduce the iteration
		// space required for the actual collision pass and improve the cache locality of the colliders.
		denseViableColliders.size = 0;
		for (int bbIdx = 0; bbIdx < collidableBBs.size; ++bbIdx) {
			if (Math.abs((obbCenter.x + motion.x) - collidableBBs.centerX[bbIdx]) > collidableBBs.extentsX[bbIdx] + aabbInLocalX)
				continue;
			if (Math.abs((obbCenter.y + motion.y) - collidableBBs.centerY[bbIdx]) > collidableBBs.extentsY[bbIdx] + aabbInLocalY)
				continue;
			if (Math.abs((obbCenter.z + motion.z) - collidableBBs.centerZ[bbIdx]) > collidableBBs.extentsZ[bbIdx] + aabbInLocalZ)
				continue;

			populateDenseViableColliders.appendFrom(collidableBBs, bbIdx);
		}

		// No collisions
		if (denseViableColliders.size == 0) {
			CollisionResponse out = new CollisionResponse();
			out.surfaceCollision = false;
			out.collisionResponse = Vec3.ZERO;
			out.normal = Vec3.ZERO;
			out.location = Vec3.ZERO;
			out.temporalResponse = 1;
			return out;
		}

		double collisionResponseX = 0.0;
		double collisionResponseY = 0.0;
		double collisionResponseZ = 0.0;

		double locationX = 0.0;
		double locationY = 0.0;
		double locationZ = 0.0;

		double normalX = 0.0;
		double normalY = 0.0;
		double normalZ = 0.0;

		boolean surfaceCollision = false;
		double temporalResponse = 1;

		Vec3 uB0 = new Vec3(rotation.m00, rotation.m10, rotation.m20).normalize();
		Vec3 uB1 = new Vec3(rotation.m01, rotation.m11, rotation.m21).normalize();
		Vec3 uB2 = new Vec3(rotation.m02, rotation.m12, rotation.m22).normalize();

		// Motion in the entity's frame.
		Vec3 motion2 = rotation.transformTransposed(motion);

		// Re-use the same manifold object per collider check.
		ContinuousSeparationManifold mf = new ContinuousSeparationManifold(uB1);

		// Apply separation maths
		for (boolean horizontalPass : Iterate.trueAndFalse) {
			boolean verticalPass = !horizontalPass || !doHorizontalPass;

			if (horizontalPass && !doHorizontalPass) {
				continue;
			}

			for (int bbIdx = 0; bbIdx < denseViableColliders.size; ++bbIdx) {
				double deltaX = obbCenter.x + collisionResponseX - denseViableColliders.centerX[bbIdx];
				double deltaY = obbCenter.y + collisionResponseY - denseViableColliders.centerY[bbIdx];
				double deltaZ = obbCenter.z + collisionResponseZ - denseViableColliders.centerZ[bbIdx];

				checkCount = 0;

				mf.reset();

				double extentsX = denseViableColliders.extentsX[bbIdx];
				double extentsY = denseViableColliders.extentsY[bbIdx];
				double extentsZ = denseViableColliders.extentsZ[bbIdx];

				// Separate along A's local axes (global XYZ)
				if (mf.separate(uA0, deltaX, extentsX, a00 * obbExtents.x + a01 * obbExtents.y + a02 * obbExtents.z, motion.x, true)
					|| mf.separate(uA1, deltaY, extentsY, a10 * obbExtents.x + a11 * obbExtents.y + a12 * obbExtents.z, motion.y, true)
					|| mf.separate(uA2, deltaZ, extentsZ, a20 * obbExtents.x + a21 * obbExtents.y + a22 * obbExtents.z, motion.z, true)) {
					continue;
				}

				Vec3 deltaEntityFrame = rotation.transformTransposed(deltaX, deltaY, deltaZ);

				// Separate along B's local axes
				if (mf.separate(uB0, deltaEntityFrame.x, extentsX * a00 + extentsY * a10 + extentsZ * a20, obbExtents.x, motion2.x, false)
					|| mf.separate(uB1, deltaEntityFrame.y, extentsX * a01 + extentsY * a11 + extentsZ * a21, obbExtents.y, motion2.y, false)
					|| mf.separate(uB2, deltaEntityFrame.z, extentsX * a02 + extentsY * a12 + extentsZ * a22, obbExtents.z, motion2.z, false)) {
					continue;
				}

				// If we reach here, the manifold has valid collision positions and normals.

				if (verticalPass && !surfaceCollision)
					surfaceCollision = true;

				double timeOfImpact = mf.getTimeOfImpact();
				boolean isTemporal = timeOfImpact > 0 && timeOfImpact < 1;

				if (!isTemporal && mf.isDiscreteCollision) {
					if (mf.stepSeparation <= entityMaxStep) {
						double sep = ContinuousSeparationManifold.withSignedEpsilon(mf.stepSeparation);
						collisionResponseX += mf.stepSeparationAxis.x * sep;
						collisionResponseY += mf.stepSeparationAxis.y * sep;
						collisionResponseZ += mf.stepSeparationAxis.z * sep;
					} else {
						double sep = ContinuousSeparationManifold.withSignedEpsilon(mf.separation);
						collisionResponseX += mf.axis.x * sep;
						collisionResponseY += mf.axis.y * sep;
						collisionResponseZ += mf.axis.z * sep;
					}
					timeOfImpact = 0;
				}

				if (timeOfImpact >= 0 && temporalResponse > timeOfImpact) {
					double scale = ContinuousSeparationManifold.withSignedEpsilon(mf.normalSeparation);
					normalX = mf.normalAxis.x * scale;
					normalY = mf.normalAxis.y * scale;
					normalZ = mf.normalAxis.z * scale;

					locationX = mf.collisionX;
					locationY = mf.collisionY;
					locationZ = mf.collisionZ;
				}

				if (isTemporal && temporalResponse > timeOfImpact) {
					temporalResponse = timeOfImpact;
				}
			}

			if (verticalPass)
				break;

			boolean noVerticalMotionResponse = temporalResponse == 1;
			boolean noVerticalCollision = collisionResponseY == 0;
			if (noVerticalCollision && noVerticalMotionResponse)
				break;

			// Re-run collisions with horizontal offset
			collisionResponseX *= 129.0 / 128.0;
			collisionResponseZ *= 129.0 / 128.0;
		}

		CollisionResponse out = new CollisionResponse();
		out.surfaceCollision = surfaceCollision;
		out.collisionResponse = new Vec3(collisionResponseX, collisionResponseY, collisionResponseZ);
		out.normal = new Vec3(normalX, normalY, normalZ);
		out.location = new Vec3(locationX, locationY, locationZ);
		out.temporalResponse = temporalResponse;

		return out;
	}

	private static class ContinuousSeparationManifold {

		static final double UNDEFINED = -1;
		double latestCollisionEntryTime = UNDEFINED;
		double earliestCollisionExitTime = Double.MAX_VALUE;
		boolean isDiscreteCollision = true;

		double collisionX;
		double collisionY;
		double collisionZ;

		final Vec3 stepSeparationAxis;
		double stepSeparation;

		Vec3 normalAxis;
		double normalSeparation;
		Vec3 axis;
		double separation;

		public ContinuousSeparationManifold(Vec3 stepSeparationAxis) {
			this.stepSeparationAxis = stepSeparationAxis;
		}

		/**
		 * @return {@code true} if not colliding.
		 */
		boolean separate(Vec3 axis, double TL, double rA, double rB, double projectedMotion, boolean axisOfObjA) {
			checkCount++;
			double distance = abs(TL);
			double diff = distance - (rA + rB);

			boolean discreteCollision = diff <= 0;
			if (!discreteCollision && signum(projectedMotion) == signum(TL))
				return true;

			double sTL = signum(TL);
			double separation = sTL * abs(diff);

			if (!discreteCollision) {
				isDiscreteCollision = false;

				// Missed on this axis, means we missed entirely.
				if (abs(separation) > abs(projectedMotion))
					return true;

				double entryTime = abs(separation) / abs(projectedMotion);
				double exitTime = (diff + abs(rA) + abs(rB)) / abs(projectedMotion);
				latestCollisionEntryTime = Math.max(entryTime, latestCollisionEntryTime);
				earliestCollisionExitTime = Math.min(exitTime, earliestCollisionExitTime);
			}

			if (axisOfObjA && distance != 0 && -(diff) <= abs(normalSeparation)) {
				normalAxis = axis;
				normalSeparation = separation;
			}

			double dot = stepSeparationAxis.dot(axis);
			if (dot != 0 && discreteCollision) {
				Vec3 cross = axis.cross(stepSeparationAxis);
				double dotSeparation = signum(dot) * TL - (rA + rB);
				double stepSeparation = -dotSeparation;

				if (!cross.equals(Vec3.ZERO)) {
					Vec3 sepVec = axis.scale(dotSeparation);
					Vec3 axisPlane = axis.cross(cross);
					Vec3 stepPlane = stepSeparationAxis.cross(cross);
					Vec3 stepSeparationVec =
						sepVec.subtract(axisPlane.scale(sepVec.dot(stepPlane) / axisPlane.dot(stepPlane)));
					stepSeparation = stepSeparationVec.length();
					if (abs(this.stepSeparation) > abs(stepSeparation) && stepSeparation != 0)
						this.stepSeparation = stepSeparation;

				} else {
					if (abs(this.stepSeparation) > stepSeparation)
						this.stepSeparation = stepSeparation;
				}
			}

			if (distance != 0 && -(diff) <= abs(this.separation)) {
				this.axis = axis;
				this.separation = separation;
				double scale = signum(TL) * (axisOfObjA ? -rA : -rB) - signum(separation) * 0.125;

				collisionX = axis.x * scale;
				collisionY = axis.y * scale;
				collisionZ = axis.z * scale;
			}

			return false;
		}

		public double getTimeOfImpact() {
			if (latestCollisionEntryTime == UNDEFINED)
				return UNDEFINED;
			if (latestCollisionEntryTime > earliestCollisionExitTime)
				return UNDEFINED;
			return latestCollisionEntryTime;
		}

		private static double withSignedEpsilon(double sep) {
			return sep + (signum(sep) * 1E-4);
		}

		public void reset() {
			// Reset the manifold.
			this.axis = null;
			this.normalAxis = null;
			this.separation = Double.MAX_VALUE;
			this.stepSeparation = Double.MAX_VALUE;
			this.normalSeparation = Double.MAX_VALUE;
			this.latestCollisionEntryTime = -1;
			this.earliestCollisionExitTime = Double.MAX_VALUE;
			this.isDiscreteCollision = true;
		}
	}
}
