package com.simibubi.create.content.logistics.trains.management.schedule;

import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleSkipCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleInstruction;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class ScheduleEntry {
    public ScheduleInstruction instruction;
    public List<List<ScheduleSkipCondition>> skipConditions;
    public List<List<ScheduleWaitCondition>> waitConditions;

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
        for (List<ScheduleSkipCondition> column : skipConditions) {
            outerSkip.add(NBTHelper.writeCompoundList(column, ScheduleSkipCondition::write));
        }
        tag.put("SkipConditions", outerSkip);
        if (!instruction.supportsConditions()) {
            return tag;
        }
        for (List<ScheduleWaitCondition> column : waitConditions) {
            outerWait.add(NBTHelper.writeCompoundList(column, ScheduleWaitCondition::write));
        }
        tag.put("Conditions", outerWait);
        return tag;
    }

    public static ScheduleEntry fromTag(CompoundTag tag) {
        ScheduleEntry entry = new ScheduleEntry();
        entry.instruction = ScheduleInstruction.fromTag(tag.getCompound("Instruction"));
        entry.skipConditions = new ArrayList<>();
        for (Tag t : tag.getList("SkipConditions", Tag.TAG_LIST)) {
            if (t instanceof ListTag list) {
                entry.skipConditions.add(NBTHelper.readCompoundList(list, ScheduleSkipCondition::fromTag));
            }
        }
        entry.waitConditions = new ArrayList<>();
        if (entry.instruction.supportsConditions()) {
            for (Tag t : tag.getList("Conditions", Tag.TAG_LIST)) {
                if (t instanceof ListTag list) {
                    entry.waitConditions.add(NBTHelper.readCompoundList(list, ScheduleWaitCondition::fromTag));
                }
            }
        }
        return entry;
    }

}