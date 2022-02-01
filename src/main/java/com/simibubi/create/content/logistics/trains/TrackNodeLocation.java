package com.simibubi.create.content.logistics.trains;

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

	public static class DiscoveredLocation extends TrackNodeLocation {

		BezierConnection turn = null;
		Vec3 normal;

		public DiscoveredLocation(double p_121865_, double p_121866_, double p_121867_) {
			super(p_121865_, p_121866_, p_121867_);
		}

		public DiscoveredLocation(Vec3 vec) {
			super(vec);
		}

		public DiscoveredLocation viaTurn(BezierConnection turn) {
			this.turn = turn;
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

	}

}
