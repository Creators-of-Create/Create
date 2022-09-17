package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StationPoweredCondition extends ScheduleWaitCondition {
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(ItemStack.EMPTY, Lang.translateDirect("schedule.condition.powered"));
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		GlobalStation currentStation = train.getCurrentStation();
		if (currentStation == null)
			return false;
		TravellingPoint leadingPoint = train.carriages.get(0).getLeadingPoint();
		if (leadingPoint.node1 == null)
			return false;
		Level dimensionLevel = level.getServer().getLevel(leadingPoint.node1.getLocation().dimension);
		BlockPos stationPos = currentStation.getTilePos();
		if (!dimensionLevel.isLoaded(stationPos))
			return false;
		return dimensionLevel.hasNeighborSignal(stationPos);
	}

	@Override
	protected void writeAdditional(CompoundTag tag) {}

	@Override
	protected void readAdditional(CompoundTag tag) {}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("powered");
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		return Lang.translateDirect("schedule.condition.powered.status");
	}
}
