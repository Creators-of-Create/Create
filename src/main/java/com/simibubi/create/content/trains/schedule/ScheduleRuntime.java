package com.simibubi.create.content.trains.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.display.GlobalTrainDisplayData.TrainDeparturePrediction;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.condition.ScheduledDelay;
import com.simibubi.create.content.trains.schedule.destination.ChangeThrottleInstruction;
import com.simibubi.create.content.trains.schedule.destination.ChangeTitleInstruction;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScheduleRuntime {

	private static final int TBD = -1;
	private static final int INVALID = -2;

	public enum State {
		PRE_TRANSIT, IN_TRANSIT, POST_TRANSIT
	}

	Train train;
	Schedule schedule;

	public boolean isAutoSchedule;
	public boolean paused;
	public boolean completed;
	public int currentEntry;
	public State state;

	static final int INTERVAL = 40;
	int cooldown;
	List<Integer> conditionProgress;
	List<CompoundTag> conditionContext;
	String currentTitle;

	int ticksInTransit;
	List<Integer> predictionTicks;

	public boolean displayLinkUpdateRequested;

	public ScheduleRuntime(Train train) {
		this.train = train;
		reset();
	}

	public void destinationReached() {
		if (state != State.IN_TRANSIT)
			return;
		state = State.POST_TRANSIT;
		conditionProgress.clear();
		displayLinkUpdateRequested = true;
		for (Carriage carriage : train.carriages)
			carriage.storage.resetIdleCargoTracker();

		if (ticksInTransit > 0) {
			int current = predictionTicks.get(currentEntry);
			if (current > 0)
				ticksInTransit = (current + ticksInTransit) / 2;
			predictionTicks.set(currentEntry, ticksInTransit);
		}

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
		if (train.navigation.destination != null) {
			ticksInTransit++;
			return;
		}
		if (currentEntry >= schedule.entries.size()) {
			currentEntry = 0;
			if (!schedule.cyclic) {
				paused = true;
				completed = true;
			}
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

		GlobalStation nextStation = startCurrentInstruction();
		if (nextStation == null)
			return;

		train.status.successfulNavigation();
		if (nextStation == train.getCurrentStation()) {
			state = State.IN_TRANSIT;
			destinationReached();
			return;
		}
		if (train.navigation.startNavigation(nextStation, Double.MAX_VALUE, false) != TBD) {
			state = State.IN_TRANSIT;
			ticksInTransit = 0;
		}
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
			int prevVersion = tag.getInt("StatusVersion");

			if (condition.tickCompletion(level, train, tag)) {
				conditionContext.set(i, new CompoundTag());
				conditionProgress.set(i, progress + 1);
				displayLinkUpdateRequested |= i == 0;
			}

			displayLinkUpdateRequested |= i == 0 && prevVersion != tag.getInt("StatusVersion");
		}

		for (Carriage carriage : train.carriages)
			carriage.storage.tickIdleCargoTracker();
	}

	public GlobalStation startCurrentInstruction() {
		ScheduleEntry entry = schedule.entries.get(currentEntry);
		ScheduleInstruction instruction = entry.instruction;

		if (instruction instanceof DestinationInstruction destination) {
			String regex = destination.getFilterForRegex();
			GlobalStation best = null;
			double bestCost = Double.MAX_VALUE;
			boolean anyMatch = false;

			if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
				train.status.missingConductor();
				cooldown = INTERVAL;
				return null;
			}

			for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
				if (!globalStation.name.matches(regex))
					continue;
				anyMatch = true;
				boolean matchesCurrent = train.currentStation != null && train.currentStation.equals(globalStation.id);
				double cost = matchesCurrent ? 0 : train.navigation.startNavigation(globalStation, bestCost, true);
				if (cost < 0)
					continue;
				if (cost > bestCost)
					continue;
				best = globalStation;
				bestCost = cost;
			}

			if (best == null) {
				if (anyMatch)
					train.status.failedNavigation();
				else
					train.status.failedNavigationNoTarget(destination.getFilter());
				cooldown = INTERVAL;
				return null;
			}

			return best;
		}

		if (instruction instanceof ChangeTitleInstruction title) {
			currentTitle = title.getScheduleTitle();
			state = State.PRE_TRANSIT;
			currentEntry++;
			return null;
		}

		if (instruction instanceof ChangeThrottleInstruction throttle) {
			train.throttle = throttle.getThrottle();
			state = State.PRE_TRANSIT;
			currentEntry++;
			return null;
		}

		return null;
	}

	public void setSchedule(Schedule schedule, boolean auto) {
		reset();
		this.schedule = schedule;
		currentEntry = Mth.clamp(schedule.savedProgress, 0, schedule.entries.size() - 1);
		paused = false;
		isAutoSchedule = auto;
		train.status.newSchedule();
		predictionTicks = new ArrayList<>();
		schedule.entries.forEach($ -> predictionTicks.add(TBD));
		displayLinkUpdateRequested = true;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void discardSchedule() {
		train.navigation.cancelNavigation();
		reset();
	}

	private void reset() {
		paused = true;
		completed = false;
		isAutoSchedule = false;
		currentEntry = 0;
		currentTitle = "";
		schedule = null;
		state = State.PRE_TRANSIT;
		conditionProgress = new ArrayList<>();
		conditionContext = new ArrayList<>();
		predictionTicks = new ArrayList<>();
	}

	public Collection<TrainDeparturePrediction> submitPredictions() {
		Collection<TrainDeparturePrediction> predictions = new ArrayList<>();
		int entryCount = schedule.entries.size();
		int accumulatedTime = 0;
		int current = currentEntry;

		// Current
		if (state == State.POST_TRANSIT || current >= entryCount) {
			GlobalStation currentStation = train.getCurrentStation();
			if (currentStation != null)
				predictions.add(createPrediction(current, currentStation.name, currentTitle, 0));
			int departureTime = estimateStayDuration(current);
			if (departureTime == INVALID)
				accumulatedTime = INVALID;
			else
				accumulatedTime += departureTime;

		} else {
			GlobalStation destination = train.navigation.destination;
			if (destination != null) {
				double speed =
					Math.min(train.throttle * train.maxSpeed(), (train.maxSpeed() + train.maxTurnSpeed()) / 2);
				int timeRemaining = (int) (train.navigation.distanceToDestination / speed) * 2;

				if (predictionTicks.size() > current && train.navigation.distanceStartedAt != 0) {
					float predictedTime = predictionTicks.get(current);
					if (predictedTime > 0) {
						predictedTime *= Mth
							.clamp(train.navigation.distanceToDestination / train.navigation.distanceStartedAt, 0, 1);
						timeRemaining = (timeRemaining + (int) predictedTime) / 2;
					}
				}

				accumulatedTime += timeRemaining;
				predictions.add(createPrediction(current, destination.name, currentTitle, accumulatedTime));

				int departureTime = estimateStayDuration(current);
				if (departureTime != INVALID)
					accumulatedTime += departureTime;
				else
					accumulatedTime = INVALID;

			} else
				predictForEntry(current, currentTitle, accumulatedTime, predictions);
		}

		// Upcoming
		String currentTitle = this.currentTitle;
		for (int i = 1; i < entryCount; i++) {
			int index = (i + current) % entryCount;
			if (index == 0 && !schedule.cyclic)
				break;

			if (schedule.entries.get(index).instruction instanceof ChangeTitleInstruction title) {
				currentTitle = title.getScheduleTitle();
				continue;
			}

			accumulatedTime = predictForEntry(index, currentTitle, accumulatedTime, predictions);
		}

		predictions.removeIf(Objects::isNull);
		return predictions;
	}

	private int predictForEntry(int index, String currentTitle, int accumulatedTime,
		Collection<TrainDeparturePrediction> predictions) {
		ScheduleEntry entry = schedule.entries.get(index);
		if (!(entry.instruction instanceof DestinationInstruction filter))
			return accumulatedTime;
		if (predictionTicks.size() <= currentEntry)
			return accumulatedTime;

		int departureTime = estimateStayDuration(index);

		if (accumulatedTime < 0) {
			predictions.add(createPrediction(index, filter.getFilter(), currentTitle, accumulatedTime));
			return Math.min(accumulatedTime, departureTime);
		}

		int predictedTime = predictionTicks.get(index);
		accumulatedTime += predictedTime;

		if (predictedTime == TBD)
			accumulatedTime = TBD;

		predictions.add(createPrediction(index, filter.getFilter(), currentTitle, accumulatedTime));

		if (accumulatedTime != TBD)
			accumulatedTime += departureTime;

		if (departureTime == INVALID)
			accumulatedTime = INVALID;

		return accumulatedTime;
	}

	private int estimateStayDuration(int index) {
		if (index >= schedule.entries.size()) {
			if (!schedule.cyclic)
				return INVALID;
			index = 0;
		}

		ScheduleEntry scheduleEntry = schedule.entries.get(index);
		Columns: for (List<ScheduleWaitCondition> list : scheduleEntry.conditions) {
			int total = 0;
			for (ScheduleWaitCondition condition : list) {
				if (!(condition instanceof ScheduledDelay wait))
					continue Columns;
				total += wait.totalWaitTicks();
			}
			return total;
		}

		return INVALID;
	}

	private TrainDeparturePrediction createPrediction(int index, String destination, String currentTitle, int time) {
		if (time == INVALID)
			return null;

		int size = schedule.entries.size();
		if (index >= size) {
			if (!schedule.cyclic)
				return new TrainDeparturePrediction(train, time, Components.literal(" "), destination);
			index %= size;
		}

		String text = currentTitle;
		if (text.isBlank()) {
			for (int i = 1; i < size; i++) {
				int j = (index + i) % size;
				ScheduleEntry scheduleEntry = schedule.entries.get(j);
				if (!(scheduleEntry.instruction instanceof DestinationInstruction instruction))
					continue;
				text = instruction.getFilter()
					.replaceAll("\\*", "")
					.trim();
				break;
			}
		}

		return new TrainDeparturePrediction(train, time, Components.literal(text), destination);
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("CurrentEntry", currentEntry);
		tag.putBoolean("AutoSchedule", isAutoSchedule);
		tag.putBoolean("Paused", paused);
		tag.putBoolean("Completed", completed);
		if (schedule != null)
			tag.put("Schedule", schedule.write());
		NBTHelper.writeEnum(tag, "State", state);
		tag.putIntArray("ConditionProgress", conditionProgress);
		tag.put("ConditionContext", NBTHelper.writeCompoundList(conditionContext, CompoundTag::copy));
		tag.putIntArray("TransitTimes", predictionTicks);
		return tag;
	}

	public void read(CompoundTag tag) {
		reset();
		paused = tag.getBoolean("Paused");
		completed = tag.getBoolean("Completed");
		isAutoSchedule = tag.getBoolean("AutoSchedule");
		currentEntry = tag.getInt("CurrentEntry");
		if (tag.contains("Schedule"))
			schedule = Schedule.fromTag(tag.getCompound("Schedule"));
		state = NBTHelper.readEnum(tag, "State", State.class);
		for (int i : tag.getIntArray("ConditionProgress"))
			conditionProgress.add(i);
		NBTHelper.iterateCompoundList(tag.getList("ConditionContext", Tag.TAG_COMPOUND), conditionContext::add);

		int[] readTransits = tag.getIntArray("TransitTimes");
		if (schedule != null) {
			schedule.entries.forEach($ -> predictionTicks.add(TBD));
			if (readTransits.length == schedule.entries.size())
				for (int i = 0; i < readTransits.length; i++)
					predictionTicks.set(i, readTransits[i]);
		}
	}

	public ItemStack returnSchedule() {
		if (schedule == null)
			return ItemStack.EMPTY;
		ItemStack stack = AllItems.SCHEDULE.asStack();
		CompoundTag nbt = stack.getOrCreateTag();
		schedule.savedProgress = currentEntry;
		nbt.put("Schedule", schedule.write());
		stack = isAutoSchedule ? ItemStack.EMPTY : stack;
		discardSchedule();
		return stack;
	}

	public void setSchedulePresentClientside(boolean present) {
		schedule = present ? new Schedule() : null;
	}

	public MutableComponent getWaitingStatus(Level level) {
		List<List<ScheduleWaitCondition>> conditions = schedule.entries.get(currentEntry).conditions;
		if (conditions.isEmpty() || conditionProgress.isEmpty() || conditionContext.isEmpty())
			return Components.empty();

		List<ScheduleWaitCondition> list = conditions.get(0);
		int progress = conditionProgress.get(0);
		if (progress >= list.size())
			return Components.empty();

		CompoundTag tag = conditionContext.get(0);
		ScheduleWaitCondition condition = list.get(progress);
		return condition.getWaitingStatus(level, train, tag);
	}

}
