package com.simibubi.create.content.logistics.trains.management.schedule;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.FilteredDestination;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleDestination;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class ScheduleRuntime {

	public enum State {
		PRE_TRANSIT, IN_TRANSIT, POST_TRANSIT
	}

	Train train;
	Schedule schedule;

	boolean isAutoSchedule;
	public boolean paused;
	public int currentEntry;
	public State state;

	static final int INTERVAL = 40;
	int cooldown;
	List<Integer> conditionProgress;
	List<CompoundTag> conditionContext;

	public ScheduleRuntime(Train train) {
		this.train = train;
		reset();
	}

	public void destinationReached() {
		if (state != State.IN_TRANSIT)
			return;
		state = State.POST_TRANSIT;
		conditionProgress.clear();
		if (currentEntry >= schedule.entries.size())
			return;
		List<List<ScheduleWaitCondition>> conditions = schedule.entries.get(currentEntry).conditions;
		for (int i = 0; i < conditions.size(); i++) {
			conditionProgress.add(0);
			conditionContext.add(new CompoundTag());
		}
	}

	public void transitInterrupted() {
		if (schedule == null || state != State.IN_TRANSIT)
			return;
		state = State.PRE_TRANSIT;
		cooldown = 0;
	}

	public void tick(Level level) {
		if (schedule == null)
			return;
		if (paused)
			return;
		if (train.derailed)
			return;
		if (train.navigation.destination != null)
			return;
		if (currentEntry >= schedule.entries.size()) {
			currentEntry = 0;
			if (!schedule.cyclic)
				paused = true;
			return;
		}

		if (cooldown-- > 0)
			return;
		if (state == State.IN_TRANSIT)
			return;
		if (state == State.POST_TRANSIT) {
			tickConditions(level);
			return;
		}

		GlobalStation nextStation = findNextStation();
		if (nextStation == null) {
			train.status.failedNavigation();
			cooldown = INTERVAL;
			return;
		}
		train.status.successfulNavigation();
		if (nextStation == train.getCurrentStation()) {
			state = State.IN_TRANSIT;
			destinationReached();
			return;
		}
		if (train.navigation.startNavigation(nextStation, Double.MAX_VALUE, false) != -1)
			state = State.IN_TRANSIT;
	}

	public void tickConditions(Level level) {
		List<List<ScheduleWaitCondition>> conditions = schedule.entries.get(currentEntry).conditions;
		for (int i = 0; i < conditions.size(); i++) {
			List<ScheduleWaitCondition> list = conditions.get(i);
			int progress = conditionProgress.get(i);

			if (progress >= list.size()) {
				state = State.PRE_TRANSIT;
				currentEntry++;
				return;
			}

			CompoundTag tag = conditionContext.get(i);
			ScheduleWaitCondition condition = list.get(progress);
			if (condition.tickCompletion(level, train, tag)) {
				conditionContext.set(i, new CompoundTag());
				conditionProgress.set(i, progress + 1);
			}
		}
	}

	public GlobalStation findNextStation() {
		ScheduleEntry entry = schedule.entries.get(currentEntry);
		ScheduleDestination destination = entry.destination;

		if (destination instanceof FilteredDestination filtered) {
			String regex = filtered.nameFilter.replace("*", ".*");
			GlobalStation best = null;
			double bestCost = Double.MAX_VALUE;
			for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
				if (!globalStation.name.matches(regex))
					continue;
				boolean matchesCurrent = train.currentStation != null && train.currentStation.equals(globalStation.id);
				double cost = matchesCurrent ? 0 : train.navigation.startNavigation(globalStation, bestCost, true);
				if (cost < 0)
					continue;
				if (cost > bestCost)
					continue;
				best = globalStation;
				bestCost = cost;
			}
			return best;
		}

		return null;
	}

	public void setSchedule(Schedule schedule, boolean auto) {
		reset();
		this.schedule = schedule;
		currentEntry = 0;
		paused = false;
		isAutoSchedule = auto;
		train.status.newSchedule();
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void discardSchedule() {
		reset();
	}

	private void reset() {
		paused = true;
		isAutoSchedule = false;
		currentEntry = 0;
		schedule = null;
		state = State.PRE_TRANSIT;
		conditionProgress = new ArrayList<>();
		conditionContext = new ArrayList<>();
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("CurrentEntry", currentEntry);
		tag.putBoolean("AutoSchedule", isAutoSchedule);
		tag.putBoolean("Paused", paused);
		if (schedule != null)
			tag.put("Schedule", schedule.write());
		NBTHelper.writeEnum(tag, "State", state);
		tag.putIntArray("ConditionProgress", conditionProgress);
		tag.put("ConditionContext", NBTHelper.writeCompoundList(conditionContext, CompoundTag::copy));
		return tag;
	}

	public void read(CompoundTag tag) {
		reset();
		paused = tag.getBoolean("Paused");
		isAutoSchedule = tag.getBoolean("AutoSchedule");
		currentEntry = tag.getInt("CurrentEntry");
		if (tag.contains("Schedule"))
			schedule = Schedule.fromTag(tag.getCompound("Schedule"));
		state = NBTHelper.readEnum(tag, "State", State.class);
		for (int i : tag.getIntArray("ConditionProgress"))
			conditionProgress.add(i);
		NBTHelper.iterateCompoundList(tag.getList("ConditionContext", Tag.TAG_COMPOUND), conditionContext::add);
	}

}
