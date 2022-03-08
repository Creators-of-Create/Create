package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;

public abstract class TrackEdgePoint {

	public UUID id;
	public Couple<TrackNodeLocation> edgeLocation;
	public double position;
	private EdgePointType<?> type;

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	public void setType(EdgePointType<?> type) {
		this.type = type;
	}

	public EdgePointType<?> getType() {
		return type;
	}

	public abstract boolean canMerge();

	public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
		return false;
	}

	public abstract void invalidate(LevelAccessor level);

	protected void invalidateAt(LevelAccessor level, BlockPos tilePos) {
		TrackTargetingBehaviour<?> behaviour = TileEntityBehaviour.get(level, tilePos, TrackTargetingBehaviour.TYPE);
		if (behaviour == null)
			return;
		CompoundTag migrationData = new CompoundTag();
		write(migrationData);
		behaviour.invalidateEdgePoint(migrationData);
	}

	public abstract void tileAdded(BlockPos tilePos, boolean front);

	public abstract void tileRemoved(BlockPos tilePos, boolean front);

	public void onRemoved(TrackGraph graph) {}

	public void setLocation(Couple<TrackNodeLocation> nodes, double position) {
		this.edgeLocation = nodes;
		this.position = position;
	}

	public double getLocationOn(TrackNode node1, TrackNode node2, TrackEdge edge) {
		return isPrimary(node1) ? edge.getLength(node1, node2) - position : position;
	}

	public boolean canNavigateVia(TrackNode side) {
		return true;
	}

	public boolean isPrimary(TrackNode node1) {
		return edgeLocation.getSecond()
			.equals(node1.getLocation());
	}

	public void read(CompoundTag nbt, boolean migration) {
		if (migration)
			return;

		id = nbt.getUUID("Id");
		position = nbt.getDouble("Position");
		edgeLocation = Couple.deserializeEach(nbt.getList("Edge", Tag.TAG_COMPOUND),
			tag -> TrackNodeLocation.fromPackedPos(NbtUtils.readBlockPos(tag)));
	}

	public void read(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		edgeLocation = Couple.create(() -> TrackNodeLocation.fromPackedPos(buffer.readBlockPos()));
		position = buffer.readDouble();
	}

	public void write(CompoundTag nbt) {
		nbt.putUUID("Id", id);
		nbt.putDouble("Position", position);
		nbt.put("Edge", edgeLocation.serializeEach(loc -> NbtUtils.writeBlockPos(new BlockPos(loc))));
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(type.getId());
		buffer.writeUUID(id);
		edgeLocation.forEach(loc -> buffer.writeBlockPos(new BlockPos(loc)));
		buffer.writeDouble(position);
	}

	public void tick(TrackGraph graph) {}

	protected void removeFromAllGraphs() {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values())
			if (trackGraph.removePoint(getType(), id) != null)
				return;
	}

}
