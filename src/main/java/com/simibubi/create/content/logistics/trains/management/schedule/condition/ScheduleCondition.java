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

public abstract class ScheduleCondition extends ScheduleDataEntry {

    public abstract boolean tickCompletion(Level level, Train train, CompoundTag context);

    protected void requestStatusToUpdate(CompoundTag context) {
        context.putInt("StatusVersion", context.getInt("StatusVersion") + 1);
    }

    public final CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", getId().toString());
        tag.put("Data", data.copy());
        writeAdditional(tag);
        return tag;
    }

    public abstract MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag);
}
