package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.FluidThresholdCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.IdleCargoCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ItemThresholdCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduledDelay;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.StationPoweredCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.StationUnloadedCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.TimeOfDayCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.FilteredDestination;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.NearestDestination;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.RedstoneDestination;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleDestination;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class Schedule {

	public static List<Pair<ResourceLocation, Supplier<? extends ScheduleDestination>>> DESTINATION_TYPES =
		new ArrayList<>();
	public static List<Pair<ResourceLocation, Supplier<? extends ScheduleWaitCondition>>> CONDITION_TYPES =
		new ArrayList<>();

	static {
		registerDestination("filtered", FilteredDestination::new);
		registerDestination("nearest", NearestDestination::new);
		registerDestination("redstone", RedstoneDestination::new);
		registerCondition("delay", ScheduledDelay::new);
		registerCondition("time_of_day", TimeOfDayCondition::new);
		registerCondition("fluid_threshold", FluidThresholdCondition::new);
		registerCondition("item_threshold", ItemThresholdCondition::new);
		registerCondition("idle", IdleCargoCondition::new);
		registerCondition("unloaded", StationUnloadedCondition::new);
		registerCondition("powered", StationPoweredCondition::new);
	}

	private static void registerDestination(String name, Supplier<? extends ScheduleDestination> factory) {
		DESTINATION_TYPES.add(Pair.of(Create.asResource(name), factory));
	}

	private static void registerCondition(String name, Supplier<? extends ScheduleWaitCondition> factory) {
		CONDITION_TYPES.add(Pair.of(Create.asResource(name), factory));
	}

	public static <T> List<? extends Component> getTypeOptions(List<Pair<ResourceLocation, T>> list) {
		String langSection = list.equals(DESTINATION_TYPES) ? "destination." : "condition.";
		return list.stream()
			.map(Pair::getFirst)
			.map(rl -> rl.getNamespace() + ".schedule." + langSection + rl.getPath())
			.map(TranslatableComponent::new)
			.toList();
	}

	public List<ScheduleEntry> entries;
	public boolean cyclic;

	public Schedule() {
		entries = new ArrayList<>();
		cyclic = true;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		ListTag list = NBTHelper.writeCompoundList(entries, ScheduleEntry::write);
		tag.put("Entries", list);
		tag.putBoolean("Cyclic", cyclic);
		return tag;
	}

	public static Schedule fromTag(CompoundTag tag) {
		Schedule schedule = new Schedule();
		schedule.entries = NBTHelper.readCompoundList(tag.getList("Entries", Tag.TAG_COMPOUND), ScheduleEntry::fromTag);
		schedule.cyclic = tag.getBoolean("Cyclic");
		return schedule;
	}

}
