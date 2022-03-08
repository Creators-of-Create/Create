package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RedstoneDestination extends ScheduleDestination {
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), new TextComponent("Redstone Pulse"));
	}

	@Override
	protected void write(CompoundTag tag) {}

	@Override
	protected void read(CompoundTag tag) {}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("redstone");
	}
}