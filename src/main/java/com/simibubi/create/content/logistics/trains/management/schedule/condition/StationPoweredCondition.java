package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StationPoweredCondition extends ScheduleWaitCondition {
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(ItemStack.EMPTY, Lang.translate("schedule.condition.powered"));
	}
	
	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		GlobalStation currentStation = train.getCurrentStation();
		if (currentStation == null)
			return false;
		BlockPos stationPos = currentStation.getTilePos();
		if (!level.isLoaded(stationPos))
			return false;
		return level.hasNeighborSignal(stationPos);
	}

	@Override
	protected void write(CompoundTag tag) {}

	@Override
	protected void read(CompoundTag tag) {}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("powered");
	}
}