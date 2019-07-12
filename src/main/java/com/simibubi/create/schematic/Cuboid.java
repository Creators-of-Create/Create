package com.simibubi.create.schematic;

import net.minecraft.util.math.BlockPos;

public class Cuboid {

	public int x;
	public int y;
	public int z;
	public int width;
	public int height;
	public int length;

	public Cuboid(BlockPos origin, BlockPos size) {
		this(origin, size.getX(), size.getY(), size.getZ());
	}

	public Cuboid(BlockPos origin, int width, int height, int length) {
		this.x = origin.getX() + ((width < 0) ? width : 0);
		this.y = origin.getY() + ((height < 0) ? height : 0);
		this.z = origin.getZ() + ((length < 0) ? length : 0);
		this.width = Math.abs(width);
		this.height = Math.abs(height);
		this.length = Math.abs(length);
	}

	public BlockPos getOrigin() {
		return new BlockPos(x, y, z);
	}

	public BlockPos getSize() {
		return new BlockPos(width, height, length);
	}

	public Cuboid clone() {
		return new Cuboid(new BlockPos(x, y, z), width, height, length);
	}

	public void move(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void centerHorizontallyOn(BlockPos pos) {
		x = pos.getX() - (width / 2);
		y = pos.getY();
		z = pos.getZ() - (length / 2);
	}

	public boolean intersects(Cuboid other) {
		return !(other.x >= x + width || other.z >= z + length || other.x + other.width <= x
				|| other.z + other.length <= z);
	}

	public boolean contains(BlockPos pos) {
		return (pos.getX() >= x && pos.getX() < x + width) && (pos.getY() >= y && pos.getY() < y + height)
				&& (pos.getZ() >= z && pos.getZ() < z + length);
	}

	public BlockPos getCenter() {
		return getOrigin().add(width / 2, height / 2, length / 2);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Cuboid && ((Cuboid) obj).getOrigin().equals(getOrigin())
				&& ((Cuboid) obj).getSize().equals(getSize());
	}

}