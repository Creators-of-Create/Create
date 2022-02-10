package com.simibubi.create.content.logistics.trains;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class TrackNodeLocation extends Vec3i {

	public TrackNodeLocation(Vec3 vec) {
		this(vec.x, vec.y, vec.z);
	}

	public TrackNodeLocation(double p_121865_, double p_121866_, double p_121867_) {
		super(Math.round(p_121865_ * 2), Math.floor(p_121866_ * 2), Math.round(p_121867_ * 2));
	}

	public static TrackNodeLocation fromPackedPos(BlockPos bufferPos) {
		return new TrackNodeLocation(bufferPos);
	}

	private TrackNodeLocation(BlockPos readBlockPos) {
		super(readBlockPos.getX(), readBlockPos.getY(), readBlockPos.getZ());
	}

	public Vec3 getLocation() {
		return new Vec3(getX() / 2f, getY() / 2f, getZ() / 2f);
	}

	@Override
	public boolean equals(Object pOther) {
		return super.equals(pOther);
	}

	@Override
	public int hashCode() {
		return (this.getY() + this.getZ() * 31) * 31 + this.getX();
	}

	public Collection<BlockPos> allAdjacent() {
		Set<BlockPos> set = new HashSet<>();
		Vec3 vec3 = getLocation();
		double step = 1 / 8f;
		for (int x : Iterate.positiveAndNegative)
			for (int y : Iterate.positiveAndNegative)
				for (int z : Iterate.positiveAndNegative)
					set.add(new BlockPos(vec3.add(x * step, y * step, z * step)));
		return set;
	}

	public static class DiscoveredLocation extends TrackNodeLocation {

		BezierConnection turn = null;
		boolean forceNode = false;
		Vec3 normal;

		public DiscoveredLocation(double p_121865_, double p_121866_, double p_121867_) {
			super(p_121865_, p_121866_, p_121867_);
		}

		public DiscoveredLocation(Vec3 vec) {
			super(vec);
		}

		public DiscoveredLocation viaTurn(BezierConnection turn) {
			this.turn = turn;
			if (turn != null)
				forceNode();
			return this;
		}
		
		public DiscoveredLocation forceNode() {
			forceNode = true;
			return this;
		}
		
		public DiscoveredLocation withNormal(Vec3 normal) {
			this.normal = normal;
			return this;
		}

		public boolean connectedViaTurn() {
			return turn != null;
		}

		public BezierConnection getTurn() {
			return turn;
		}
		
		public boolean shouldForceNode() {
			return forceNode;
		}

	}

}
