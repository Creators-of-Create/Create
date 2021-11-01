package com.simibubi.create.foundation.collision;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.world.phys.Vec3;

public class OBBCollider {

	static final Vec3 uA0 = new Vec3(1, 0, 0);
	static final Vec3 uA1 = new Vec3(0, 1, 0);
	static final Vec3 uA2 = new Vec3(0, 0, 1);

	public static Vec3 separateBBs(Vec3 cA, Vec3 cB, Vec3 eA, Vec3 eB, Matrix3d m) {
		SeparationManifold mf = new SeparationManifold();

		Vec3 t = cB.subtract(cA);

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

	static boolean isSeparatedAlong(SeparationManifold mf, Vec3 axis, double TL, double rA, double rB) {
		checkCount++;
		double distance = abs(TL);
		double diff = distance - (rA + rB);
		if (diff > 0)
			return true;

		boolean isBestSeperation = checkCount == 2; // Debug specific separations

		if (isBestSeperation) {
			double sTL = signum(TL);
			double value = sTL * abs(diff);
			mf.axis = axis.normalize();
			mf.separation = value;
		}

		return false;
	}

	static class SeparationManifold {
		Vec3 axis;
		double separation;

		public SeparationManifold() {
			axis = Vec3.ZERO;
			separation = Double.MAX_VALUE;
		}

		public Vec3 asSeparationVec() {
			double sep = separation;
			Vec3 axis = this.axis;
			return createSeparationVec(sep, axis);
		}

		protected Vec3 createSeparationVec(double sep, Vec3 axis) {
			return axis.normalize()
				.scale(signum(sep) * (abs(sep) + 1E-4));
		}
	}

}
