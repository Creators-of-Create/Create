package com.simibubi.create.foundation.ponder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;

public abstract class Selection implements Predicate<BlockPos> {

	public static Selection of(MutableBoundingBox bb) {
		return new Simple(bb);
	}

	public abstract Selection add(Selection other);

	public abstract Selection substract(Selection other);

	public abstract Selection copy();

	public abstract Vector3d getCenter();

	public abstract void forEach(Consumer<BlockPos> callback);

	public abstract OutlineParams makeOutline(Outliner outliner, Object slot);

	public OutlineParams makeOutline(Outliner outliner) {
		return makeOutline(outliner, this);
	}

	private static class Compound extends Selection {

		Set<BlockPos> posSet;
		Vector3d center;

		public Compound(Simple initial) {
			posSet = new HashSet<>();
			add(initial);
		}

		private Compound(Set<BlockPos> template) {
			posSet = new HashSet<>(template);
		}

		@Override
		public boolean test(BlockPos t) {
			return posSet.contains(t);
		}

		@Override
		public Selection add(Selection other) {
			other.forEach(p -> posSet.add(p.toImmutable()));
			center = null;
			return this;
		}

		@Override
		public Selection substract(Selection other) {
			other.forEach(p -> posSet.remove(p.toImmutable()));
			center = null;
			return this;
		}

		@Override
		public void forEach(Consumer<BlockPos> callback) {
			posSet.forEach(callback);
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner, Object slot) {
			return outliner.showCluster(slot, posSet);
		}

		@Override
		public Vector3d getCenter() {
			return center == null ? center = evalCenter() : center;
		}

		private Vector3d evalCenter() {
			Vector3d center = Vector3d.ZERO;
			if (posSet.isEmpty())
				return center;
			for (BlockPos blockPos : posSet)
				center = center.add(Vector3d.of(blockPos));
			center = center.scale(1f / posSet.size());
			return center.add(new Vector3d(.5, .5, .5));
		}

		@Override
		public Selection copy() {
			return new Compound(posSet);
		}

	}

	private static class Simple extends Selection {

		private MutableBoundingBox bb;
		private AxisAlignedBB aabb;

		public Simple(MutableBoundingBox bb) {
			this.bb = bb;
			this.aabb = getAABB();
		}

		@Override
		public boolean test(BlockPos t) {
			return bb.isVecInside(t);
		}

		@Override
		public Selection add(Selection other) {
			return new Compound(this).add(other);
		}

		@Override
		public Selection substract(Selection other) {
			return new Compound(this).substract(other);
		}

		@Override
		public void forEach(Consumer<BlockPos> callback) {
			BlockPos.stream(bb)
				.forEach(callback);
		}

		@Override
		public Vector3d getCenter() {
			return aabb.getCenter();
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner, Object slot) {
			return outliner.showAABB(slot, aabb);
		}

		private AxisAlignedBB getAABB() {
			return new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX + 1, bb.maxY + 1, bb.maxZ + 1);
		}

		@Override
		public Selection copy() {
			return new Simple(new MutableBoundingBox(bb));
		}

	}

}
