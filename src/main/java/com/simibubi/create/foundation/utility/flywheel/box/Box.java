package com.simibubi.create.foundation.utility.flywheel.box;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public interface Box {
	int getMinX();

	int getMinY();

	int getMinZ();

	int getMaxX();

	int getMaxY();

	int getMaxZ();

	default int sizeX() {
		return getMaxX() - getMinX();
	}

	default int sizeY() {
		return getMaxY() - getMinY();
	}

	default int sizeZ() {
		return getMaxZ() - getMinZ();
	}

	default int volume() {
		return sizeX() * sizeY() * sizeZ();
	}

	default boolean isEmpty() {
		// if any dimension has side length 0 this box contains no volume
		return getMinX() == getMaxX() || getMinY() == getMaxY() || getMinZ() == getMaxZ();
	}

	default boolean sameAs(Box other) {
		return getMinX() == other.getMinX() && getMinY() == other.getMinY() && getMinZ() == other.getMinZ() && getMaxX() == other.getMaxX() && getMaxY() == other.getMaxY() && getMaxZ() == other.getMaxZ();
	}

	default boolean sameAs(Box other, int margin) {
		return getMinX() == other.getMinX() - margin &&
				getMinY() == other.getMinY() - margin &&
				getMinZ() == other.getMinZ() - margin &&
				getMaxX() == other.getMaxX() + margin &&
				getMaxY() == other.getMaxY() + margin &&
				getMaxZ() == other.getMaxZ() + margin;
	}

	default boolean sameAs(AABB other) {
		return getMinX() == Math.floor(other.minX)
				&& getMinY() == Math.floor(other.minY)
				&& getMinZ() == Math.floor(other.minZ)
				&& getMaxX() == Math.ceil(other.maxX)
				&& getMaxY() == Math.ceil(other.maxY)
				&& getMaxZ() == Math.ceil(other.maxZ);
	}

	default boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return this.getMinX() < maxX && this.getMaxX() > minX && this.getMinY() < maxY && this.getMaxY() > minY && this.getMinZ() < maxZ && this.getMaxZ() > minZ;
	}

	default boolean intersects(Box other) {
		return this.intersects(other.getMinX(), other.getMinY(), other.getMinZ(), other.getMaxX(), other.getMaxY(), other.getMaxZ());
	}

	default boolean contains(int x, int y, int z) {
		return x >= getMinX()
				&& x <= getMaxX()
				&& y >= getMinY()
				&& y <= getMaxY()
				&& z >= getMinZ()
				&& z <= getMaxZ();
	}

	default boolean contains(Box other) {
		return other.getMinX() >= this.getMinX()
				&& other.getMaxX() <= this.getMaxX()
				&& other.getMinY() >= this.getMinY()
				&& other.getMaxY() <= this.getMaxY()
				&& other.getMinZ() >= this.getMinZ()
				&& other.getMaxZ() <= this.getMaxZ();
	}

	default void forEachContained(CoordinateConsumer func) {
		int minX = getMinX();
		int minY = getMinY();
		int minZ = getMinZ();
		int maxX = getMaxX();
		int maxY = getMaxY();
		int maxZ = getMaxZ();

		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					func.accept(x, y, z);
				}
			}
		}
	}

	default boolean hasPowerOf2Sides() {
		// this is only true if all individual side lengths are powers of 2
		return Mth.isPowerOfTwo(volume());
	}

	default MutableBox union(Box other) {
		int minX = Math.min(this.getMinX(), other.getMinX());
		int minY = Math.min(this.getMinY(), other.getMinY());
		int minZ = Math.min(this.getMinZ(), other.getMinZ());
		int maxX = Math.max(this.getMaxX(), other.getMaxX());
		int maxY = Math.max(this.getMaxY(), other.getMaxY());
		int maxZ = Math.max(this.getMaxZ(), other.getMaxZ());
		return new MutableBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	default MutableBox intersect(Box other) {
		int minX = Math.max(this.getMinX(), other.getMinX());
		int minY = Math.max(this.getMinY(), other.getMinY());
		int minZ = Math.max(this.getMinZ(), other.getMinZ());
		int maxX = Math.min(this.getMaxX(), other.getMaxX());
		int maxY = Math.min(this.getMaxY(), other.getMaxY());
		int maxZ = Math.min(this.getMaxZ(), other.getMaxZ());
		return new MutableBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	default AABB toAABB() {
		return new AABB(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
	}

	default MutableBox copy() {
		return new MutableBox(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
	}

	@FunctionalInterface
	interface CoordinateConsumer {
		void accept(int x, int y, int z);
	}
}
