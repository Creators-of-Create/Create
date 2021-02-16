package com.simibubi.create.foundation.ponder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public abstract class Select implements Predicate<BlockPos> {

	public static Select cuboid(BlockPos origin, Vec3i size) {
		return new Cuboid(origin, size);
	}

	public static Select pos(int x, int y, int z) {
		return new Cuboid(new BlockPos(x, y, z), BlockPos.ZERO);
	}

	public static Select fromTo(int x, int y, int z, int x2, int y2, int z2) {
		return new Cuboid(new BlockPos(x, y, z), new BlockPos(x2 - x, y2 - y, z2 - z));
	}

	public static Select everything(MutableBoundingBox bounds) {
		return cuboid(BlockPos.ZERO, bounds.getLength());
	}

	public static Select compound(Select... other) {
		return new Compound(other);
	}

	public static Select column(MutableBoundingBox bounds, int x, int z) {
		return cuboid(new BlockPos(x, 1, z), new Vec3i(0, bounds.getYSize(), 0));
	}

	public static Select layer(MutableBoundingBox bounds, int y, int height) {
		return cuboid(new BlockPos(0, y, 0),
			new Vec3i(bounds.getXSize(), Math.min(bounds.getYSize() - y, height) - 1, bounds.getZSize()));
	}

	//

	public WorldSectionElement asElement() {
		return new WorldSectionElement(this);
	}

	//

	@Override
	public abstract int hashCode();

	public abstract Stream<BlockPos> all();

	public abstract Vec3d getCenter();

	public abstract OutlineParams makeOutline(Outliner outliner);

	private static class Compound extends Select {

		private Select[] parts;
		private int hash;
		private Vec3d center;
		private Set<BlockPos> cluster;

		public Compound(Select... parts) {
			this.parts = parts;
			if (parts.length == 0)
				throw new IllegalArgumentException("Cannot instantiate Compound Select with zero parts");

			cluster = new HashSet<>();
			parts[0].all()
				.map(BlockPos::toImmutable)
				.forEach(cluster::add);
			hash = parts[0].hashCode();
			center = parts[0].getCenter()
				.scale(1f / parts.length);
			for (int i = 1; i < parts.length; i++) {
				Select select = parts[i];
				select.all()
					.map(BlockPos::toImmutable)
					.forEach(cluster::add);
				hash = hash ^ select.hashCode();
				center = center.add(select.getCenter()
					.scale(1f / parts.length));
			}
		}

		@Override
		public boolean test(BlockPos t) {
			for (Select select : parts)
				if (select.test(t))
					return true;
			return false;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public Stream<BlockPos> all() {
			return cluster.stream();
		}

		@Override
		public Vec3d getCenter() {
			return center;
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner) {
			return outliner.showCluster(this, cluster);
		}

	}

	private static class Cuboid extends Select {

		MutableBoundingBox bb;
		Vec3i origin;
		Vec3i size;

		public Cuboid(BlockPos origin, Vec3i size) {
			bb = new MutableBoundingBox(origin, origin.add(size));
			this.origin = origin;
			this.size = size;
		}

		@Override
		public boolean test(BlockPos t) {
			return bb.isVecInside(t);
		}

		@Override
		public Stream<BlockPos> all() {
			return BlockPos.func_229383_a_(bb);
		}

		@Override
		public int hashCode() {
			return origin.hashCode() ^ size.hashCode();
		}

		@Override
		public Vec3d getCenter() {
			return new AxisAlignedBB(new BlockPos(origin), new BlockPos(origin).add(size)
				.add(1, 1, 1)).getCenter();
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner) {
			return outliner.showAABB(this, new AxisAlignedBB(new BlockPos(origin), new BlockPos(origin).add(size)
				.add(1, 1, 1)));
		}

	}

}
