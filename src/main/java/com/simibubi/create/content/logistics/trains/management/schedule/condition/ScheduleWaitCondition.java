package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class ScheduleWaitCondition extends ScheduleCondition {

	public static ScheduleWaitCondition fromTag(CompoundTag tag) {
		ResourceLocation location = new ResourceLocation(tag.getString("Id"));
		Supplier<? extends ScheduleWaitCondition> supplier = null;
		for (Pair<ResourceLocation, Supplier<? extends ScheduleWaitCondition>> pair : Schedule.WAIT_CONDITION_TYPES)
			if (pair.getFirst()
					.equals(location))
				supplier = pair.getSecond();

		if (supplier == null) {
			Create.LOGGER.warn("Could not parse waiting condition type: " + location);
			return null;
		}

		ScheduleWaitCondition condition = supplier.get();
		condition.data = tag.getCompound("Data");
		condition.readAdditional(tag);
		return condition;
	}
}
