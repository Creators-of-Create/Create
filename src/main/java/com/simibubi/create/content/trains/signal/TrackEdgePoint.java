package com.simibubi.create.content.trains.signal;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.DimensionPalette;
import com.simibubi.create.content.trains.edgePoint.EdgePointType;
import com.simibubi.create.content.trains.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

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

	protected void invalidateAt(LevelAccessor level, BlockPos blockEntityPos) {
		TrackTargetingBehaviour<?> behaviour = BlockEntityBehaviour.get(level, blockEntityPos, TrackTargetingBehaviour.TYPE);
		if (behaviour == null)
			return;
		CompoundTag migrationData = new CompoundTag();
		DimensionPalette dimensions = new DimensionPalette();
		write(migrationData, dimensions);
		dimensions.write(migrationData);
		behaviour.invalidateEdgePoint(migrationData);
	}

	public abstract void blockEntityAdded(BlockEntity blockEntity, boolean front);

	public abstract void blockEntityRemoved(BlockPos blockEntityPos, boolean front);

	public void onRemoved(TrackGraph graph) {}

	public void setLocation(Couple<TrackNodeLocation> nodes, double position) {
		this.edgeLocation = nodes;
		this.position = position;
	}

	public double getLocationOn(TrackEdge edge) {
		return isPrimary(edge.node1) ? edge.getLength() - position : position;
	}

	public boolean canNavigateVia(TrackNode side) {
		return true;
	}

	public boolean isPrimary(TrackNode node1) {
		return edgeLocation.getSecond()
			.equals(node1.getLocation());
	}

	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		if (migration)
			return;

		id = nbt.getUUID("Id");
		position = nbt.getDouble("Position");
		edgeLocation = Couple.deserializeEach(nbt.getList("Edge", Tag.TAG_COMPOUND),
			tag -> TrackNodeLocation.read(tag, dimensions));
	}

	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		id = buffer.readUUID();
		edgeLocation = Couple.create(() -> TrackNodeLocation.receive(buffer, dimensions));
		position = buffer.readDouble();
	}

	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		nbt.putUUID("Id", id);
		nbt.putDouble("Position", position);
		nbt.put("Edge", edgeLocation.serializeEach(loc -> loc.write(dimensions)));
	}

	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		buffer.writeResourceLocation(type.getId());
		buffer.writeUUID(id);
		edgeLocation.forEach(loc -> loc.send(buffer, dimensions));
		buffer.writeDouble(position);
	}

	public void tick(TrackGraph graph, boolean preTrains) {}

	protected void removeFromAllGraphs() {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values())
			if (trackGraph.removePoint(getType(), id) != null)
				return;
	}

}
