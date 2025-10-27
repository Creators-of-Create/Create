package com.simibubi.create.foundation.utility.flywheel.box;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.Collection;

public class MutableBox implements Box {
	protected int minX;
	protected int minY;
	protected int minZ;
	protected int maxX;
	protected int maxY;
	protected int maxZ;

	public MutableBox() {
	}

	public MutableBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public static MutableBox from(AABB aabb) {
		int minX = (int) Math.floor(aabb.minX);
		int minY = (int) Math.floor(aabb.minY);
		int minZ = (int) Math.floor(aabb.minZ);
		int maxX = (int) Math.ceil(aabb.maxX);
		int maxY = (int) Math.ceil(aabb.maxY);
		int maxZ = (int) Math.ceil(aabb.maxZ);
		return new MutableBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static MutableBox from(Vec3i pos) {
		return new MutableBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public static MutableBox from(SectionPos pos) {
		return new MutableBox(pos.minBlockX(), pos.minBlockY(), pos.minBlockZ(), pos.maxBlockX() + 1, pos.maxBlockY() + 1, pos.maxBlockZ() + 1);
	}

	public static MutableBox from(Vec3i start, Vec3i end) {
		return new MutableBox(start.getX(), start.getY(), start.getZ(), end.getX() + 1, end.getY() + 1, end.getZ() + 1);
	}

	public static MutableBox ofRadius(int radius) {
		return new MutableBox(-radius, -radius, -radius, radius + 1, radius + 1, radius + 1);
	}

	public static Box containingAll(Collection<BlockPos> positions) {
		if (positions.isEmpty()) {
			return new MutableBox();
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : positions) {
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}
		return new MutableBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public int getMinX() {
		return minX;
	}

	@Override
	public int getMinY() {
		return minY;
	}

	@Override
	public int getMinZ() {
		return minZ;
	}

	@Override
	public int getMaxX() {
		return maxX;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public int getMaxZ() {
		return maxZ;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public MutableBox setMinZ(int minZ) {
		this.minZ = minZ;
		return this;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public void setMaxZ(int maxZ) {
		this.maxZ = maxZ;
	}

	public void setMin(int x, int y, int z) {
		minX = x;
		minY = y;
		minZ = z;
	}

	public void setMax(int x, int y, int z) {
		maxX = x;
		maxY = y;
		maxZ = z;
	}

	public void setMin(Vec3i v) {
		setMin(v.getX(), v.getY(), v.getZ());
	}

	public void setMax(Vec3i v) {
		setMax(v.getX(), v.getY(), v.getZ());
	}

	public void assign(Box other) {
		minX = other.getMinX();
		minY = other.getMinY();
		minZ = other.getMinZ();
		maxX = other.getMaxX();
		maxY = other.getMaxY();
		maxZ = other.getMaxZ();
	}

	public void assign(AABB other) {
		minX = (int) Math.floor(other.minX);
		minY = (int) Math.floor(other.minY);
		minZ = (int) Math.floor(other.minZ);
		maxX = (int) Math.ceil(other.maxX);
		maxY = (int) Math.ceil(other.maxY);
		maxZ = (int) Math.ceil(other.maxZ);
	}

	public void assign(Vec3i start, Vec3i end) {
		minX = start.getX();
		minY = start.getY();
		minZ = start.getZ();
		maxX = end.getX() + 1;
		maxY = end.getY() + 1;
		maxZ = end.getZ() + 1;
	}

	public void unionAssign(Box other) {
		minX = Math.min(this.minX, other.getMinX());
		minY = Math.min(this.minY, other.getMinY());
		minZ = Math.min(this.minZ, other.getMinZ());
		maxX = Math.max(this.maxX, other.getMaxX());
		maxY = Math.max(this.maxY, other.getMaxY());
		maxZ = Math.max(this.maxZ, other.getMaxZ());
	}

	public void unionAssign(AABB other) {
		minX = Math.min(this.minX, (int) Math.floor(other.minX));
		minY = Math.min(this.minY, (int) Math.floor(other.minY));
		minZ = Math.min(this.minZ, (int) Math.floor(other.minZ));
		maxX = Math.max(this.maxX, (int) Math.ceil(other.maxX));
		maxY = Math.max(this.maxY, (int) Math.ceil(other.maxY));
		maxZ = Math.max(this.maxZ, (int) Math.ceil(other.maxZ));
	}

	public void intersectAssign(Box other) {
		minX = Math.max(this.minX, other.getMinX());
		minY = Math.max(this.minY, other.getMinY());
		minZ = Math.max(this.minZ, other.getMinZ());
		maxX = Math.min(this.maxX, other.getMaxX());
		maxY = Math.min(this.maxY, other.getMaxY());
		maxZ = Math.min(this.maxZ, other.getMaxZ());
	}

	public void fixMinMax() {
		int minX = Math.min(this.minX, this.maxX);
		int minY = Math.min(this.minY, this.maxY);
		int minZ = Math.min(this.minZ, this.maxZ);
		int maxX = Math.max(this.minX, this.maxX);
		int maxY = Math.max(this.minY, this.maxY);
		int maxZ = Math.max(this.minZ, this.maxZ);

		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public void translate(int x, int y, int z) {
		minX = minX + x;
		maxX = maxX + x;
		minY = minY + y;
		maxY = maxY + y;
		minZ = minZ + z;
		maxZ = maxZ + z;
	}

	public void translate(Vec3i by) {
		translate(by.getX(), by.getY(), by.getZ());
	}

	public void grow(int x, int y, int z) {
		minX = minX - x;
		minY = minY - y;
		minZ = minZ - z;
		maxX = maxX + x;
		maxY = maxY + y;
		maxZ = maxZ + z;
	}

	public void grow(int s) {
		this.grow(s, s, s);
	}

	/**
	 * Grow this box to have power of 2 side lengths, scaling from the minimum coords.
	 */
	public void nextPowerOf2() {
		int sizeX = Mth.smallestEncompassingPowerOfTwo(sizeX());
		int sizeY = Mth.smallestEncompassingPowerOfTwo(sizeY());
		int sizeZ = Mth.smallestEncompassingPowerOfTwo(sizeZ());

		maxX = minX + sizeX;
		maxY = minY + sizeY;
		maxZ = minZ + sizeZ;
	}

	/**
	 * Grow this box to have power of 2 side length, scaling from the center.
	 */
	public void nextPowerOf2Centered() {
		int sizeX = sizeX();
		int sizeY = sizeY();
		int sizeZ = sizeZ();

		int newSizeX = Mth.smallestEncompassingPowerOfTwo(sizeX);
		int newSizeY = Mth.smallestEncompassingPowerOfTwo(sizeY);
		int newSizeZ = Mth.smallestEncompassingPowerOfTwo(sizeZ);

		int diffX = newSizeX - sizeX;
		int diffY = newSizeY - sizeY;
		int diffZ = newSizeZ - sizeZ;

		minX = minX - diffX / 2; // floor division for the minimums
		minY = minY - diffY / 2;
		minZ = minZ - diffZ / 2;
		maxX = maxX + (diffX + 1) / 2; // ceiling divison for the maximums
		maxY = maxY + (diffY + 1) / 2;
		maxZ = maxZ + (diffZ + 1) / 2;
	}

	public void mirrorAbout(Direction.Axis axis) {
		Vec3i axisVec = Direction.get(Direction.AxisDirection.POSITIVE, axis)
				.getNormal();
		int flipX = axisVec.getX() - 1;
		int flipY = axisVec.getY() - 1;
		int flipZ = axisVec.getZ() - 1;

		int maxX = this.maxX * flipX;
		int maxY = this.maxY * flipY;
		int maxZ = this.maxZ * flipZ;
		this.maxX = this.minX * flipX;
		this.maxY = this.minY * flipY;
		this.maxZ = this.minZ * flipZ;
		this.minX = maxX;
		this.minY = maxY;
		this.minZ = maxZ;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Box that)) return false;

		return this.sameAs(that);
	}

	@Override
	public int hashCode() {
		int result = minX;
		result = 31 * result + minY;
		result = 31 * result + minZ;
		result = 31 * result + maxX;
		result = 31 * result + maxY;
		result = 31 * result + maxZ;
		return result;
	}

	@Override
	public String toString() {
		return "(" + minX + ", " + minY + ", " + minZ + ")->(" + maxX + ", " + maxY + ", " + maxZ + ')';
	}
}
