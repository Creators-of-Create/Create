package com.simibubi.create.content.trains.observer;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrackObserver extends SingleBlockEntityEdgePoint {

	private int activated;
	private FilterItemStack filter;
	private UUID currentTrain;

	public TrackObserver() {
		activated = 0;
		filter = FilterItemStack.empty();
		currentTrain = null;
	}

	@Override
	public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
		super.blockEntityAdded(blockEntity, front);
		FilteringBehaviour filteringBehaviour = BlockEntityBehaviour.get(blockEntity, FilteringBehaviour.TYPE);
		if (filteringBehaviour != null)
			setFilterAndNotify(blockEntity.getLevel(), filteringBehaviour.getFilter());
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
		this.filter = FilterItemStack.of(filter.copy());
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

	public FilterItemStack getFilter() {
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
		filter = FilterItemStack.of(nbt.getCompound("Filter"));
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
