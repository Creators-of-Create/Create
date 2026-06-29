package com.simibubi.create.foundation.collision;

import net.minecraft.world.phys.shapes.Shapes.DoubleLineConsumer;

/**
 * Plaid ol' structure of arrays containing dense bounding box data for collision checks.
 */
public class CollisionList {

	public static final int DEFAULT_CAPACITY = 16;

	public double[] centerX = new double[DEFAULT_CAPACITY];
	public double[] centerY = new double[DEFAULT_CAPACITY];
	public double[] centerZ = new double[DEFAULT_CAPACITY];
	public double[] extentsX = new double[DEFAULT_CAPACITY];
	public double[] extentsY = new double[DEFAULT_CAPACITY];
	public double[] extentsZ = new double[DEFAULT_CAPACITY];

	public int size = 0;

	/**
	 * Helper to populate a CollisionList. Feed this into {@link net.minecraft.world.phys.shapes.VoxelShape#forAllBoxes}.
	 */
	public static class Populate implements DoubleLineConsumer {
		private final CollisionList collisionList;

		public int offsetX = 0;
		public int offsetY = 0;
		public int offsetZ = 0;

		public Populate(CollisionList collisionList) {
			this.collisionList = collisionList;
		}

		@Override
		public void consume(double x1, double y1, double z1, double x2, double y2, double z2) {
			// These are the values we'll be using so precompute them.
			append(offsetX + 0.5 * (x2 + x1),
			offsetY + 0.5 * (y2 + y1),
			offsetZ + 0.5 * (z2 + z1),
			0.5 * (x2 - x1),
			0.5 * (y2 - y1),
			0.5 * (z2 - z1));
		}

		public void append(double centerX, double centerY, double centerZ, double extentsX, double extentsY, double extentsZ) {
			// Out of space, must reallocate.
			if (collisionList.size == collisionList.centerX.length) {
				int newCapacity = collisionList.centerX.length * 2;
				double[] newCenterX = new double[newCapacity];
				double[] newCenterY = new double[newCapacity];
				double[] newCenterZ = new double[newCapacity];
				double[] newExtentsX = new double[newCapacity];
				double[] newExtentsY = new double[newCapacity];
				double[] newExtentsZ = new double[newCapacity];

				System.arraycopy(collisionList.centerX, 0, newCenterX, 0, collisionList.size);
				System.arraycopy(collisionList.centerY, 0, newCenterY, 0, collisionList.size);
				System.arraycopy(collisionList.centerZ, 0, newCenterZ, 0, collisionList.size);
				System.arraycopy(collisionList.extentsX, 0, newExtentsX, 0, collisionList.size);
				System.arraycopy(collisionList.extentsY, 0, newExtentsY, 0, collisionList.size);
				System.arraycopy(collisionList.extentsZ, 0, newExtentsZ, 0, collisionList.size);

				collisionList.centerX = newCenterX;
				collisionList.centerY = newCenterY;
				collisionList.centerZ = newCenterZ;
				collisionList.extentsX = newExtentsX;
				collisionList.extentsY = newExtentsY;
				collisionList.extentsZ = newExtentsZ;
			}

			collisionList.centerX[collisionList.size] = centerX;
			collisionList.centerY[collisionList.size] = centerY;
			collisionList.centerZ[collisionList.size] = centerZ;
			collisionList.extentsX[collisionList.size] = extentsX;
			collisionList.extentsY[collisionList.size] = extentsY;
			collisionList.extentsZ[collisionList.size] = extentsZ;

			++collisionList.size;
		}

		public void appendFrom(CollisionList collisionList, int bbIdx) {
			append(collisionList.centerX[bbIdx], collisionList.centerY[bbIdx], collisionList.centerZ[bbIdx], collisionList.extentsX[bbIdx], collisionList.extentsY[bbIdx], collisionList.extentsZ[bbIdx]);
		}
	}
}
