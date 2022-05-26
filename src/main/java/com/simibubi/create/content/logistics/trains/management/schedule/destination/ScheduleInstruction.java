package com.simibubi.create.content.logistics.trains.management.schedule.destination;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleDataEntry;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class ScheduleInstruction extends ScheduleDataEntry {

	public abstract boolean supportsConditions();

	public final CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Id", getId().toString());
		tag.put("Data", data.copy());
		writeAdditional(tag);
		return tag;
	}

	public static ScheduleInstruction fromTag(CompoundTag tag) {
		ResourceLocation location = new ResourceLocation(tag.getString("Id"));
		Supplier<? extends ScheduleInstruction> supplier = null;
		for (Pair<ResourceLocation, Supplier<? extends ScheduleInstruction>> pair : Schedule.INSTRUCTION_TYPES)
			if (pair.getFirst()
				.equals(location))
				supplier = pair.getSecond();

		if (supplier == null) {
			Create.LOGGER.warn("Could not parse schedule instruction type: " + location);
			return new DestinationInstruction();
		}

		ScheduleInstruction scheduleDestination = supplier.get();
		scheduleDestination.data = tag.getCompound("Data");
		scheduleDestination.readAdditional(tag);
		return scheduleDestination;
	}

}