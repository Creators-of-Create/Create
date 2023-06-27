package com.simibubi.create.foundation.ponder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.foundation.outliner.Outline.OutlineParams;
import com.simibubi.create.foundation.outliner.Outliner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Selection implements Predicate<BlockPos> {

	public static Selection of(BoundingBox bb) {
		return new Simple(bb);
	}

	public abstract Selection add(Selection other);

	public abstract Selection substract(Selection other);

	public abstract Selection copy();

	public abstract Vec3 getCenter();

	public abstract void forEach(Consumer<BlockPos> callback);

	public abstract OutlineParams makeOutline(Outliner outliner, Object slot);

	public OutlineParams makeOutline(Outliner outliner) {
		return makeOutline(outliner, this);
	}

	private static class Compound extends Selection {

		Set<BlockPos> posSet;
		Vec3 center;

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
			other.forEach(p -> posSet.add(p.immutable()));
			center = null;
			return this;
		}

		@Override
		public Selection substract(Selection other) {
			other.forEach(p -> posSet.remove(p.immutable()));
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
		public Vec3 getCenter() {
			return center == null ? center = evalCenter() : center;
		}

		private Vec3 evalCenter() {
			Vec3 center = Vec3.ZERO;
			if (posSet.isEmpty())
				return center;
			for (BlockPos blockPos : posSet)
				center = center.add(Vec3.atLowerCornerOf(blockPos));
			center = center.scale(1f / posSet.size());
			return center.add(new Vec3(.5, .5, .5));
		}

		@Override
		public Selection copy() {
			return new Compound(posSet);
		}

	}

	private static class Simple extends Selection {

		private BoundingBox bb;
		private AABB aabb;

		public Simple(BoundingBox bb) {
			this.bb = bb;
			this.aabb = getAABB();
		}

		@Override
		public boolean test(BlockPos t) {
			return bb.isInside(t);
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
			BlockPos.betweenClosedStream(bb)
				.forEach(callback);
		}

		@Override
		public Vec3 getCenter() {
			return aabb.getCenter();
		}

		@Override
		public OutlineParams makeOutline(Outliner outliner, Object slot) {
			return outliner.showAABB(slot, aabb);
		}

		private AABB getAABB() {
			return new AABB(bb.minX(), bb.minY(), bb.minZ(), bb.maxX() + 1, bb.maxY() + 1, bb.maxZ() + 1);
		}

		@Override
		public Selection copy() {
			return new Simple(new BoundingBox(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ()));
		}

	}

}
