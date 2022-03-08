package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class NearestDestination extends ScheduleDestination {
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), Lang.translate("schedule.destination.nearest"));
	}

	@Override
	protected void write(CompoundTag tag) {}

	@Override
	protected void read(CompoundTag tag) {}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("nearest");
	}
}