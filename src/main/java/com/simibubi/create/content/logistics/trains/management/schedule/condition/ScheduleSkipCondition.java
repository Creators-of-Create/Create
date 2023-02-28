package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleDataEntry;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public abstract class ScheduleSkipCondition extends ScheduleCondition {

	public static ScheduleSkipCondition fromTag(CompoundTag tag) {
		ResourceLocation location = new ResourceLocation(tag.getString("Id"));
		Supplier<? extends ScheduleSkipCondition> supplier = null;
		for (Pair<ResourceLocation, Supplier<? extends ScheduleSkipCondition>> pair : Schedule.SKIP_CONDITION_TYPES)
			if (pair.getFirst()
				.equals(location))
				supplier = pair.getSecond();

		if (supplier == null) {
			Create.LOGGER.warn("Could not parse waiting condition type: " + location);
			return null;
		}

		ScheduleSkipCondition condition = supplier.get();
		condition.data = tag.getCompound("Data");
		condition.readAdditional(tag);
		return condition;
	}
}