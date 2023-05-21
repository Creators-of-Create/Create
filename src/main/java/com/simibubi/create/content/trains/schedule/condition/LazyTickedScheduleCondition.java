package com.simibubi.create.content.trains.schedule.condition;

import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public abstract class LazyTickedScheduleCondition extends ScheduleWaitCondition {

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
