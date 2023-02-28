package com.simibubi.create.content.logistics.trains.management.schedule;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleSkipCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.skip.TimeOfDaySkipCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.FluidThresholdCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.IdleCargoCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.ItemThresholdCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.PlayerPassengerCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.RedstoneLinkCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.ScheduledDelay;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.StationPoweredCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.StationUnloadedCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.wait.TimeOfDayWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ChangeThrottleInstruction;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ChangeTitleInstruction;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleInstruction;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Schedule {

    public static List<Pair<ResourceLocation, Supplier<? extends ScheduleInstruction>>> INSTRUCTION_TYPES =
            new ArrayList<>();
    public static List<Pair<ResourceLocation, Supplier<? extends ScheduleWaitCondition>>> WAIT_CONDITION_TYPES =
            new ArrayList<>();
    public static List<Pair<ResourceLocation, Supplier<? extends ScheduleSkipCondition>>> SKIP_CONDITION_TYPES =
            new ArrayList<>();

    static {
        registerInstruction("destination", DestinationInstruction::new);
        registerInstruction("rename", ChangeTitleInstruction::new);
        registerInstruction("throttle", ChangeThrottleInstruction::new);
        registerWaitCondition("delay", ScheduledDelay::new);
        registerWaitCondition("time_of_day", TimeOfDayWaitCondition::new);
        registerWaitCondition("fluid_threshold", FluidThresholdCondition::new);
        registerWaitCondition("item_threshold", ItemThresholdCondition::new);
        registerWaitCondition("redstone_link", RedstoneLinkCondition::new);
        registerWaitCondition("player_count", PlayerPassengerCondition::new);
        registerWaitCondition("idle", IdleCargoCondition::new);
        registerWaitCondition("unloaded", StationUnloadedCondition::new);
        registerWaitCondition("powered", StationPoweredCondition::new);
        registerSkipCondition("time_of_day", TimeOfDaySkipCondition::new);
    }

    private static void registerInstruction(String name, Supplier<? extends ScheduleInstruction> factory) {
        INSTRUCTION_TYPES.add(Pair.of(Create.asResource(name), factory));
    }

    private static void registerWaitCondition(String name, Supplier<? extends ScheduleWaitCondition> factory) {
        WAIT_CONDITION_TYPES.add(Pair.of(Create.asResource(name), factory));
    }

    private static void registerSkipCondition(String name, Supplier<? extends ScheduleSkipCondition> factory) {
        SKIP_CONDITION_TYPES.add(Pair.of(Create.asResource(name), factory));
    }

    public static <T> List<? extends Component> getTypeOptions(List<Pair<ResourceLocation, T>> list) {
        String langSection = list.equals(INSTRUCTION_TYPES) ? "instruction." : "condition.";
        return list.stream()
                .map(Pair::getFirst)
                .map(rl -> rl.getNamespace() + ".schedule." + langSection + rl.getPath())
                .map(Components::translatable)
                .toList();
    }

    public List<ScheduleEntry> entries;
    public boolean cyclic;
    public int savedProgress;

    public Schedule() {
        entries = new ArrayList<>();
        cyclic = true;
        savedProgress = 0;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ListTag list = NBTHelper.writeCompoundList(entries, ScheduleEntry::write);
        tag.put("Entries", list);
        tag.putBoolean("Cyclic", cyclic);
        if (savedProgress > 0) {
            tag.putInt("Progress", savedProgress);
        }
        return tag;
    }

    public static Schedule fromTag(CompoundTag tag) {
        Schedule schedule = new Schedule();
        schedule.entries = NBTHelper.readCompoundList(tag.getList("Entries", Tag.TAG_COMPOUND), ScheduleEntry::fromTag);
        schedule.cyclic = tag.getBoolean("Cyclic");
        if (tag.contains("Progress")) {
            schedule.savedProgress = tag.getInt("Progress");
        }
        return schedule;
    }

}
