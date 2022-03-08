package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.IScheduleInput;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class ScheduleWaitCondition implements IScheduleInput {

	protected abstract void write(CompoundTag tag);

	protected abstract void read(CompoundTag tag);

	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return false; // TODO: make abstract
	}
	
	public final CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Id", getId().toString());
		write(tag);
		return tag;
	}

	public static ScheduleWaitCondition fromTag(CompoundTag tag) {
		ResourceLocation location = new ResourceLocation(tag.getString("Id"));
		Supplier<? extends ScheduleWaitCondition> supplier = null;
		for (Pair<ResourceLocation, Supplier<? extends ScheduleWaitCondition>> pair : Schedule.CONDITION_TYPES)
			if (pair.getFirst()
				.equals(location))
				supplier = pair.getSecond();

		if (supplier == null) {
			Create.LOGGER.warn("Could not parse waiting condition type: " + location);
			return null;
		}

		ScheduleWaitCondition condition = supplier.get();
		condition.read(tag);
		return condition;
	}

}