package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleDestination;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ScheduleEntry {
	public ScheduleDestination destination;
	public List<List<ScheduleWaitCondition>> conditions;

	public ScheduleEntry() {
		conditions = new ArrayList<>();
	}

	public ScheduleEntry clone() {
		return fromTag(write());
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		ListTag outer = new ListTag();
		tag.put("Destination", destination.write());
		for (List<ScheduleWaitCondition> column : conditions)
			outer.add(NBTHelper.writeCompoundList(column, ScheduleWaitCondition::write));
		tag.put("WaitConditions", outer);
		return tag;
	}

	public static ScheduleEntry fromTag(CompoundTag tag) {
		ScheduleEntry entry = new ScheduleEntry();
		entry.destination = ScheduleDestination.fromTag(tag.getCompound("Destination"));
		entry.conditions = new ArrayList<>();
		for (Tag t : tag.getList("WaitConditions", Tag.TAG_LIST))
			if (t instanceof ListTag list)
				entry.conditions.add(NBTHelper.readCompoundList(list, ScheduleWaitCondition::fromTag));
		return entry;
	}

}