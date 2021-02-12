package com.simibubi.create.foundation.metadoc;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.simibubi.create.foundation.metadoc.elements.WorldSectionElement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3i;

public abstract class Select implements Predicate<BlockPos> {

	public static Select cuboid(BlockPos origin, Vec3i size) {
		return new Cuboid(origin, size);
	}
	
	public static Select pos(int x, int y, int z) {
		return new Cuboid(new BlockPos(x, y, z), BlockPos.ZERO);
	}

	public static Select everything(MetaDocScene scene) {
		MutableBoundingBox bounds = scene.getBounds();
		return cuboid(BlockPos.ZERO, bounds.getLength());
	}

	//
	
	public WorldSectionElement asElement() {
		return new WorldSectionElement(this); 
	}
	
	//

	@Override
	public abstract int hashCode();

	public abstract Stream<BlockPos> all();

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

	}

}
