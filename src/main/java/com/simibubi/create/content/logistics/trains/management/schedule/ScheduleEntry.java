package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleInstruction;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ScheduleEntry {
	public ScheduleInstruction instruction;
	public List<List<ScheduleCondition>> skipConditions;
	public List<List<ScheduleCondition>> waitConditions;

	public ScheduleEntry() {
		skipConditions = new ArrayList<>();
		waitConditions = new ArrayList<>();
	}

	public ScheduleEntry clone() {
		return fromTag(write());
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		ListTag outerSkip = new ListTag();
		ListTag outerWait = new ListTag();
		tag.put("Instruction", instruction.write());
		for (List<ScheduleCondition> column : skipConditions) {
			System.out.println("ScheduleEntry.write: " + column);
			outerSkip.add(NBTHelper.writeCompoundList(column, ScheduleCondition::write));
		}
		tag.put("SkipConditions", outerSkip);
		if (!instruction.supportsConditions()) {
			return tag;
		}
		for (List<ScheduleCondition> column : waitConditions) {
			System.out.println("ScheduleEntry.write: " + column);
			outerWait.add(NBTHelper.writeCompoundList(column, ScheduleCondition::write));
		}
		tag.put("WaitConditions", outerWait);
		return tag;
	}

	public static ScheduleEntry fromTag(CompoundTag tag) {
		ScheduleEntry entry = new ScheduleEntry();
		entry.instruction = ScheduleInstruction.fromTag(tag.getCompound("Instruction"));
		entry.skipConditions = new ArrayList<>();
		for (Tag t : tag.getList("SkipConditions", Tag.TAG_LIST)) {
			if (t instanceof ListTag list) {
				entry.skipConditions.add(NBTHelper.readCompoundList(list, ScheduleCondition::fromTag));

				System.out.println("ScheduleEntry.fromTag: " + entry.skipConditions + " skipConditions");
			}
		}
		entry.waitConditions = new ArrayList<>();
		if (entry.instruction.supportsConditions()) {
			// Backwards compatibility
			for (Tag t : tag.contains("WaitConditions") ? tag.getList("WaitConditions", Tag.TAG_LIST) : tag.getList("Conditions", Tag.TAG_LIST)) {
				if (t instanceof ListTag list) {
					entry.waitConditions.add(NBTHelper.readCompoundList(list, ScheduleCondition::fromTag));

					System.out.println("ScheduleEntry.fromTag: " + entry.waitConditions + " waitConditions");
				}
			}
		}
		return entry;
	}

}
