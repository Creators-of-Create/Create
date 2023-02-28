package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleCondition;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public abstract class LazyTickedScheduleCondition extends ScheduleCondition {

	private int tickRate;

	public LazyTickedScheduleCondition(int tickRate) {
		this.tickRate = tickRate;
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int time = context.getInt("Time");
		if (time % tickRate == 0) {
			if (lazyTickCompletion(level, train, context))
				return true;
			time = 0;
		}
		context.putInt("Time", time + 1);
		return false;
	}

	protected abstract boolean lazyTickCompletion(Level level, Train train, CompoundTag context);

}
