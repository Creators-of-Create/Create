package com.simibubi.create.foundation.collision;

import static com.simibubi.create.foundation.collision.CollisionDebugger.showDebugLine;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.util.math.vector.Vector3d;



public class OBBCollider {

	static final Vector3d uA0 = new Vector3d(1, 0, 0);
	static final Vector3d uA1 = new Vector3d(0, 1, 0);
	static final Vector3d uA2 = new Vector3d(0, 0, 1);

	public static Vector3d separateBBs(Vector3d cA, Vector3d cB, Vector3d eA, Vector3d eB, Matrix3d m) {
		SeparationManifold mf = new SeparationManifold();

		Vector3d t = cB.subtract(cA);

		double a00 = abs(m.m00);
		double a01 = abs(m.m01);
		double a02 = abs(m.m02);
		double a10 = abs(m.m10);
		double a11 = abs(m.m11);
		double a12 = abs(m.m12);
		double a20 = abs(m.m20);
		double a21 = abs(m.m21);
		double a22 = abs(m.m22);

		Vector3d uB0 = new Vector3d(m.m00, m.m10, m.m20);
		Vector3d uB1 = new Vector3d(m.m01, m.m11, m.m21);
		Vector3d uB2 = new Vector3d(m.m02, m.m12, m.m22);

		checkCount = 0;

		if (
		// Separate along A's local axes (global XYZ)
		!(isSeparatedAlong(mf, uA0, t.x, eA.x, a00 * eB.x + a01 * eB.y + a02 * eB.z)
			|| isSeparatedAlong(mf, uA1, t.y, eA.y, a10 * eB.x + a11 * eB.y + a12 * eB.z)
			|| isSeparatedAlong(mf, uA2, t.z, eA.z, a20 * eB.x + a21 * eB.y + a22 * eB.z)

			// Separate along B's local axes
			|| isSeparatedAlong(mf, uB0, (t.x * m.m00 + t.y * m.m10 + t.z * m.m20),
				eA.x * a00 + eA.y * a10 + eA.z * a20, eB.x)
			|| isSeparatedAlong(mf, uB1, (t.x * m.m01 + t.y * m.m11 + t.z * m.m21),
				eA.x * a01 + eA.y * a11 + eA.z * a21, eB.y)
			|| isSeparatedAlong(mf, uB2, (t.x * m.m02 + t.y * m.m12 + t.z * m.m22),
				eA.x * a02 + eA.y * a12 + eA.z * a22, eB.z)))
			return mf.asSeparationVec();

		return null;
	}

	static int checkCount = 0;

	static boolean isSeparatedAlong(SeparationManifold mf, Vector3d axis, double TL, double rA, double rB) {
		checkCount++;
		double distance = abs(TL);
		double diff = distance - (rA + rB);
		if (diff > 0)
			return true;

//		boolean isBestSeperation = distance != 0 && -(diff) <= abs(bestSeparation.getValue());
		boolean isBestSeperation = checkCount == 2; // Debug specific separations

		if (isBestSeperation) {
			double sTL = signum(TL);
			double value = sTL * abs(diff);
			mf.axis = axis.normalize();
			mf.separation = value;

			// Visualize values
			if (CollisionDebugger.AABB != null) {
				Vector3d normalizedAxis = axis.normalize();
				showDebugLine(Vector3d.ZERO, normalizedAxis.scale(TL), 0xbb00bb, "tl", 4);
				showDebugLine(Vector3d.ZERO, normalizedAxis.scale(sTL * rA), 0xff4444, "ra", 3);
				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)), normalizedAxis.scale(TL), 0x4444ff, "rb", 2);
				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)),
					normalizedAxis.scale(sTL * (distance - rB) + value), 0xff9966, "separation", 1);
				System.out.println("TL:" + TL + ", rA: " + rA + ", rB: " + rB);
			}
		}

		return false;
	}

	static class SeparationManifold {
		Vector3d axis;
		double separation;

		public SeparationManifold() {
			axis = Vector3d.ZERO;
			separation = Double.MAX_VALUE;
		}

		public Vector3d asSeparationVec() {
			double sep = separation;
			Vector3d axis = this.axis;
			return createSeparationVec(sep, axis);
		}

		protected Vector3d createSeparationVec(double sep, Vector3d axis) {
			return axis.normalize()
				.scale(signum(sep) * (abs(sep) + 1E-4));
		}
	}

}
