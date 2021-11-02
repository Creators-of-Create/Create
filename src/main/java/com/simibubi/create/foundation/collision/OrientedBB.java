package com.simibubi.create.foundation.collision;

import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OrientedBB {

	Vec3 center;
	Vec3 extents;
	Matrix3d rotation;

	public OrientedBB(AABB bb) {
		this(bb.getCenter(), extentsFromBB(bb), new Matrix3d().asIdentity());
	}

	public OrientedBB() {
		this(Vec3.ZERO, Vec3.ZERO, new Matrix3d().asIdentity());
	}

	public OrientedBB(Vec3 center, Vec3 extents, Matrix3d rotation) {
		this.setCenter(center);
		this.extents = extents;
		this.setRotation(rotation);
	}

	public OrientedBB copy() {
		return new OrientedBB(center, extents, rotation);
	}

	public Vec3 intersect(AABB bb) {
		Vec3 extentsA = extentsFromBB(bb);
		Vec3 intersects = OBBCollider.separateBBs(bb.getCenter(), center, extentsA, extents, rotation);
		return intersects;
	}

	public ContinuousSeparationManifold intersect(AABB bb, Vec3 motion) {
		Vec3 extentsA = extentsFromBB(bb);
		return ContinuousOBBCollider.separateBBs(bb.getCenter(), center, extentsA, extents, rotation, motion);
	}

	private static Vec3 extentsFromBB(AABB bb) {
		return new Vec3(bb.getXsize() / 2, bb.getYsize() / 2, bb.getZsize() / 2);
	}

	public Matrix3d getRotation() {
		return rotation;
	}

	public void setRotation(Matrix3d rotation) {
		this.rotation = rotation;
	}

	public Vec3 getCenter() {
		return center;
	}

	public void setCenter(Vec3 center) {
		this.center = center;
	}

	public void move(Vec3 offset) {
		setCenter(getCenter().add(offset));
	}

	public AABB getAsAABB() {
		return new AABB(0, 0, 0, 0, 0, 0).move(center)
			.inflate(extents.x, extents.y, extents.z);
	}

	/*
	 * The following checks (edge-to-edge) need special separation logic. They are
	 * not necessary as long as the obb is only rotated around one axis at a time
	 * (Which is the case for contraptions at the moment)
	 *
	 */

	// Separate along axes perpendicular to AxB
//		|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB0), t.z * m.m10 - t.y * m.m20,
//			eA.y * a20 + eA.z * a10, eB.y * a02 + eB.z * a01)
//		|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB1), t.z * m.m11 - t.y * m.m21,
//			eA.y * a21 + eA.z * a11, eB.x * a02 + eB.z * a00)
//		|| isSeparatedAlong(bestAxis, bestSep, uA0.crossProduct(uB2), t.z * m.m12 - t.y * m.m22,
//			eA.y * a22 + eA.z * a12, eB.x * a01 + eB.y * a00)
//
//		|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB0), t.x * m.m20 - t.z * m.m00,
//			eA.x * a20 + eA.z * a00, eB.y * a12 + eB.z * a11)
//		|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB1), t.x * m.m21 - t.z * m.m01,
//			eA.x * a21 + eA.z * a01, eB.x * a12 + eB.z * a10)
//		|| isSeparatedAlong(bestAxis, bestSep, uA1.crossProduct(uB2), t.x * m.m22 - t.z * m.m02,
//			eA.x * a22 + eA.z * a02, eB.x * a11 + eB.y * a10)
//
//		|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB0), t.y * m.m00 - t.x * m.m10,
//			eA.x * a10 + eA.y * a00, eB.y * a22 + eB.z * a21)
//		|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB1), t.y * m.m01 - t.x * m.m11,
//			eA.x * a11 + eA.y * a01, eB.x * a22 + eB.z * a20)
//		|| isSeparatedAlong(bestAxis, bestSep, uA2.crossProduct(uB2), t.y * m.m02 - t.x * m.m12,
//			eA.x * a12 + eA.y * a02, eB.x * a21 + eB.y * a20)

}
