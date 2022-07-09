package com.simibubi.create.content.logistics.trains.management.edgePoint.observer;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.DimensionPalette;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalPropagator;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SingleTileEdgePoint;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrackObserver extends SingleTileEdgePoint {

	private int activated;
	private ItemStack filter;
	private UUID currentTrain;

	public TrackObserver() {
		activated = 0;
		filter = ItemStack.EMPTY;
		currentTrain = null;
	}

	@Override
	public void tileAdded(BlockEntity tile, boolean front) {
		super.tileAdded(tile, front);
		FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get(tile, FilteringBehaviour.TYPE);
		if (filteringBehaviour != null)
			setFilterAndNotify(tile.getLevel(), filteringBehaviour.getFilter());
	}

	@Override
	public void tick(TrackGraph graph, boolean preTrains) {
		super.tick(graph, preTrains);
		if (isActivated())
			activated--;
		if (!isActivated())
			currentTrain = null;
	}

	public void setFilterAndNotify(Level level, ItemStack filter) {
		this.filter = filter;
		notifyTrains(level);
	}

	private void notifyTrains(Level level) {
		TrackGraph graph = Create.RAILWAYS.sided(level)
			.getGraph(level, edgeLocation.getFirst());
		if (graph == null)
			return;
		TrackEdge edge = graph.getConnection(edgeLocation.map(graph::locateNode));
		if (edge == null)
			return;
		SignalPropagator.notifyTrains(graph, edge);
	}

	public ItemStack getFilter() {
		return filter;
	}
	
	public UUID getCurrentTrain() {
		return currentTrain;
	}

	public boolean isActivated() {
		return activated > 0;
	}

	public void keepAlive(Train train) {
		activated = 8;
		currentTrain = train.id;
	}

	@Override
	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		super.read(nbt, migration, dimensions);
		activated = nbt.getInt("Activated");
		filter = ItemStack.of(nbt.getCompound("Filter"));
		if (nbt.contains("TrainId"))
			currentTrain = nbt.getUUID("TrainId");
	}

	@Override
	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
	}

	@Override
	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		super.write(nbt, dimensions);
		nbt.putInt("Activated", activated);
		nbt.put("Filter", filter.serializeNBT());
		if (currentTrain != null)
			nbt.putUUID("TrainId", currentTrain);
	}

	@Override
	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
	}

}
