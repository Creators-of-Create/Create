package com.simibubi.create.foundation.collision;

import static java.lang.Math.abs;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class OrientedBB {

	Vec3d center;
	Vec3d extents;
	Matrix3d rotation;

	public OrientedBB(AxisAlignedBB bb) {
		this(bb.getCenter(), extentsFromBB(bb), new Matrix3d().asIdentity());
	}

	public OrientedBB() {
		this(Vec3d.ZERO, Vec3d.ZERO, new Matrix3d().asIdentity());
	}

	public OrientedBB(Vec3d center, Vec3d extents, Matrix3d rotation) {
		this.setCenter(center);
		this.extents = extents;
		this.setRotation(rotation);
	}

	public Vec3d intersect(AxisAlignedBB bb) {
		Vec3d extentsA = extentsFromBB(bb);
		// Inverse rotation, to bring our OBB to AA space
		Vec3d intersects = separateBBs(bb.getCenter(), center, extentsA, extents, rotation.transpose());
		// clean up
		rotation.transpose();
		return intersects;
	}

	private static Vec3d extentsFromBB(AxisAlignedBB bb) {
		return new Vec3d(bb.getXSize() / 2, bb.getYSize() / 2, bb.getZSize() / 2);
	}

	public static Vec3d separateBBs(Vec3d cA, Vec3d cB, Vec3d eA, Vec3d eB, Matrix3d m) {
		Vec3d t = cB.subtract(cA);
		double a00 = abs(m.m00);
		double a01 = abs(m.m01);
		double a02 = abs(m.m02);
		double a10 = abs(m.m10);
		double a11 = abs(m.m11);
		double a12 = abs(m.m12);
		double a20 = abs(m.m20);
		double a21 = abs(m.m21);
		double a22 = abs(m.m22);

		MutableObject<Vec3d> bestAxis = new MutableObject<>(Vec3d.ZERO);
		MutableDouble bestSep = new MutableDouble(Double.MAX_VALUE);

		Vec3d uA0 = new Vec3d(1, 0, 0);
		Vec3d uA1 = new Vec3d(0, 1, 0);
		Vec3d uA2 = new Vec3d(0, 0, 1);

		Vec3d uB0 = new Vec3d(m.m00, m.m01, m.m02);
		Vec3d uB1 = new Vec3d(m.m10, m.m11, m.m12);
		Vec3d uB2 = new Vec3d(m.m20, m.m21, m.m22);

		checkCount = 0;

		if (

		// Separate along A's local axes (global XYZ)
		!(isSeparatedAlong(bestAxis, bestSep, uA0, t.x, eA.x, a00 * eB.x + a01 * eB.y + a02 * eB.z)
			|| isSeparatedAlong(bestAxis, bestSep, uA1, t.y, eA.y, a10 * eB.x + a11 * eB.y + a12 * eB.z)
			|| isSeparatedAlong(bestAxis, bestSep, uA2, t.z, eA.z, a20 * eB.x + a21 * eB.y + a22 * eB.z)

			// Separate along B's local axes
			|| isSeparatedAlong(bestAxis, bestSep, uB0, t.x * m.m00 + t.y * m.m10 + t.z * m.m20,
				eA.x * a00 + eA.y * a10 + eA.z * a20, eB.x)
			|| isSeparatedAlong(bestAxis, bestSep, uB1, t.x * m.m01 + t.y * m.m11 + t.z * m.m21,
				eA.x * a01 + eA.y * a11 + eA.z * a21, eB.y)
			|| isSeparatedAlong(bestAxis, bestSep, uB2, t.x * m.m02 + t.y * m.m12 + t.z * m.m22,
				eA.x * a02 + eA.y * a12 + eA.z * a22, eB.z)

			// Separate along axes perpendicular to AxB
			|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB0), t.z * m.m10 - t.y * m.m20,
				eA.y * a20 + eA.z * a10, eB.y * a02 + eB.z * a01)
			|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB1), t.z * m.m11 - t.y * m.m21,
				eA.y * a21 + eA.z * a11, eB.x * a02 + eB.z * a00)
			|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB2), t.z * m.m12 - t.y * m.m22,
				eA.y * a22 + eA.z * a12, eB.x * a01 + eB.y * a00)

			|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB0), t.x * m.m20 - t.z * m.m00,
				eA.x * a20 + eA.z * a00, eB.y * a12 + eB.z * a11)
			|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB1), t.x * m.m21 - t.z * m.m01,
				eA.x * a21 + eA.z * a01, eB.x * a12 + eB.z * a10)
			|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB2), t.x * m.m22 - t.z * m.m02,
				eA.x * a22 + eA.z * a02, eB.x * a11 + eB.y * a10)

			|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB0), t.y * m.m00 - t.x * m.m10,
				eA.x * a10 + eA.y * a00, eB.y * a22 + eB.z * a21)
			|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB1), t.y * m.m01 - t.x * m.m11,
				eA.x * a11 + eA.y * a01, eB.x * a22 + eB.z * a20)
			|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB2), t.y * m.m02 - t.x * m.m12,
				eA.x * a12 + eA.y * a02, eB.x * a21 + eB.y * a20)))

			return bestAxis.getValue()
				.normalize()
				.scale(bestSep.getValue());

		return null;
	}

	static int checkCount = 0;

	static boolean isSeparatedAlong(MutableObject<Vec3d> bestAxis, MutableDouble bestSeparation, Vec3d axis, double TL,
		double rA, double rB) {
		double distance = abs(TL);

		checkCount++;

		double diff = distance - (rA + rB);
		if (diff > 0)
			return true;
		if (distance != 0 && -diff < abs(bestSeparation.getValue())) {
			bestAxis.setValue(axis);
			bestSeparation.setValue(Math.signum(TL) * abs(diff));
		}

		return false;
	}

	public Matrix3d getRotation() {
		return rotation;
	}
	
	public void setRotation(Matrix3d rotation) {
		this.rotation = rotation;
	}

	public Vec3d getCenter() {
		return center;
	}
	
	public void setCenter(Vec3d center) {
		this.center = center;
	}

}
