package com.simibubi.create.content.logistics.trains;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TrackNodeLocation extends Vec3i {

	public ResourceKey<Level> dimension;

	public TrackNodeLocation(Vec3 vec) {
		this(vec.x, vec.y, vec.z);
	}

	public TrackNodeLocation(double p_121865_, double p_121866_, double p_121867_) {
		super(Math.round(p_121865_ * 2), Math.floor(p_121866_ * 2), Math.round(p_121867_ * 2));
	}

	public TrackNodeLocation in(Level level) {
		return in(level.dimension());
	}

	public TrackNodeLocation in(ResourceKey<Level> dimension) {
		this.dimension = dimension;
		return this;
	}

	private static TrackNodeLocation fromPackedPos(BlockPos bufferPos) {
		return new TrackNodeLocation(bufferPos);
	}

	private TrackNodeLocation(BlockPos readBlockPos) {
		super(readBlockPos.getX(), readBlockPos.getY(), readBlockPos.getZ());
	}

	public Vec3 getLocation() {
		return new Vec3(getX() / 2f, getY() / 2f, getZ() / 2f);
	}

	public ResourceKey<Level> getDimension() {
		return dimension;
	}

	@Override
	public boolean equals(Object pOther) {
		return equalsIgnoreDim(pOther) && pOther instanceof TrackNodeLocation tnl
			&& Objects.equals(tnl.dimension, dimension);
	}
	
	public boolean equalsIgnoreDim(Object pOther) {
		return super.equals(pOther);
	}

	@Override
	public int hashCode() {
		return (this.getY() + (this.getZ() * 31 + dimension.hashCode()) * 31) * 31 + this.getX();
	}

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag c = NbtUtils.writeBlockPos(new BlockPos(this));
		if (dimensions != null)
			c.putInt("D", dimensions.encode(dimension));
		return c;
	}

	public static TrackNodeLocation read(CompoundTag tag, DimensionPalette dimensions) {
		TrackNodeLocation location = fromPackedPos(NbtUtils.readBlockPos(tag));
		if (dimensions != null)
			location.dimension = dimensions.decode(tag.getInt("D"));
		return location;
	}

	public void send(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		buffer.writeBlockPos(new BlockPos(this));
		buffer.writeVarInt(dimensions.encode(dimension));
	}

	public static TrackNodeLocation receive(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		TrackNodeLocation location = fromPackedPos(buffer.readBlockPos());
		location.dimension = dimensions.decode(buffer.readVarInt());
		return location;
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
		Vec3 direction;
		Vec3 normal;

		public DiscoveredLocation(Level level, double p_121865_, double p_121866_, double p_121867_) {
			super(p_121865_, p_121866_, p_121867_);
			in(level);
		}

		public DiscoveredLocation(ResourceKey<Level> dimension, Vec3 vec) {
			super(vec);
			in(dimension);
		}

		public DiscoveredLocation(Level level, Vec3 vec) {
			this(level.dimension(), vec);
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

		public DiscoveredLocation withDirection(Vec3 direction) {
			this.direction = direction == null ? null : direction.normalize();
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

		public boolean notInLineWith(Vec3 direction) {
			return this.direction != null
				&& Math.max(direction.dot(this.direction), direction.dot(this.direction.scale(-1))) < 7 / 8f;
		}

	}

}
