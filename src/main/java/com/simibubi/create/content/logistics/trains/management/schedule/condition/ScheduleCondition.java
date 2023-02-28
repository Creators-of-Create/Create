package com.simibubi.create.content.logistics.trains.management.schedule.condition;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleDataEntry;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class ScheduleCondition extends ScheduleDataEntry {

	private ScheduleConditionType type = ScheduleConditionType.UNDEFINED;

	public abstract boolean tickCompletion(Level level, Train train, CompoundTag context);

	protected void requestStatusToUpdate(CompoundTag context) {
		context.putInt("StatusVersion", context.getInt("StatusVersion") + 1);
	}

	public final CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Id", getId().toString());
		tag.put("Data", data.copy());
		tag.putInt("Type", type.ordinal());
		writeAdditional(tag);
		return tag;
	}


	public static ScheduleCondition fromTag(CompoundTag tag) {
		ResourceLocation location = new ResourceLocation(tag.getString("Id"));
		Supplier<? extends ScheduleCondition> supplier = null;
		for (Pair<ResourceLocation, Supplier<? extends ScheduleCondition>> pair : Schedule.getAllConditions())
			if (pair.getFirst()
					.equals(location))
				supplier = pair.getSecond();

		if (supplier == null) {
			Create.LOGGER.warn("Could not parse waiting condition type: " + location);
			return null;
		}

		ScheduleCondition condition = supplier.get();
		condition.data = tag.getCompound("Data");
		System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEE");
		// Backwards compatibility
		ScheduleConditionType[] enumConstants = ScheduleConditionType.class.getEnumConstants();
		condition.type = tag.contains("Type") ? enumConstants[tag.getInt("Type") % enumConstants.length] : ScheduleConditionType.WAIT;
		// End backwards compatibility
		System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEE " + condition.type + " " + tag.contains("Type") + " " + tag.getString("Type"));
		condition.readAdditional(tag);
		return condition;
	}

	public abstract MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag);

	public ScheduleConditionType getType() {
		return this.type;
	}

	public void setType(ScheduleConditionType type) {
		this.type = type;
	}
}
