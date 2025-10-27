package com.simibubi.create.content.trains.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.display.GlobalTrainDisplayData.TrainDeparturePrediction;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.condition.ScheduledDelay;
import com.simibubi.create.content.trains.schedule.destination.ChangeTitleInstruction;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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

	public Train train;
	public Schedule schedule;

	public boolean isAutoSchedule;
	public boolean paused;
	public boolean completed;
	public int currentEntry;
	public State state;

	public List<Integer> conditionProgress;
	public List<CompoundTag> conditionContext;
	public String currentTitle;

	public int ticksInTransit;
	public List<Integer> predictionTicks;

	public boolean displayLinkUpdateRequested;

	private static final int INTERVAL = 40;
	private int cooldown;

	public ScheduleRuntime(Train train) {
		this.train = train;
		reset();
	}

	public void startCooldown() {
		cooldown = INTERVAL;
	}

	public void destinationReached() {
		if (state != State.IN_TRANSIT)
			return;
		state = State.POST_TRANSIT;
		conditionProgress.clear();
		conditionContext.clear();
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

		if (checkEndOfScheduleReached())
			return;
		if (cooldown-- > 0)
			return;
		if (state == State.IN_TRANSIT)
			return;
		if (state == State.POST_TRANSIT) {
			tickConditions(level);
			return;
		}

		DiscoveredPath nextPath = startCurrentInstruction(level);
		if (nextPath == null)
			return;

		train.status.successfulNavigation();
		if (nextPath.destination == train.getCurrentStation()) {
			state = State.IN_TRANSIT;
			destinationReached();
			return;
		}
		if (train.navigation.startNavigation(nextPath) != TBD) {
			state = State.IN_TRANSIT;
			ticksInTransit = 0;
		}
	}

	private boolean checkEndOfScheduleReached() {
		if (currentEntry < schedule.entries.size())
			return false;

		currentEntry = 0;
		if (!schedule.cyclic) {
			paused = true;
			completed = true;
		}
		return true;
	}

	public void tickConditions(Level level) {
		ScheduleEntry entry = schedule.entries.get(currentEntry);
		List<List<ScheduleWaitCondition>> conditions = entry.conditions;

		if (!entry.instruction.supportsConditions()) {
			state = State.PRE_TRANSIT;
			currentEntry++;
			return;
		}

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

	public DiscoveredPath startCurrentInstruction(Level level) {
		if (checkEndOfScheduleReached())
			return null;

		ScheduleEntry entry = schedule.entries.get(currentEntry);
		ScheduleInstruction instruction = entry.instruction;
		return instruction.start(this, level);
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
		Columns:
		for (List<ScheduleWaitCondition> list : scheduleEntry.conditions) {
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
			if (!schedule.cyclic) {
				return new TrainDeparturePrediction(train, time, CommonComponents.space(), destination);
			}
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

		return new TrainDeparturePrediction(train, time, Component.literal(text), destination);
	}

	public CompoundTag write(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("CurrentEntry", currentEntry);
		tag.putBoolean("AutoSchedule", isAutoSchedule);
		tag.putBoolean("Paused", paused);
		tag.putBoolean("Completed", completed);
		if (schedule != null)
			tag.put("Schedule", schedule.write(registries));
		NBTHelper.writeEnum(tag, "State", state);
		tag.putIntArray("ConditionProgress", conditionProgress);
		tag.put("ConditionContext", NBTHelper.writeCompoundList(conditionContext, CompoundTag::copy));
		tag.putIntArray("TransitTimes", predictionTicks);
		return tag;
	}

	public void read(HolderLookup.Provider registries, CompoundTag tag) {
		reset();
		paused = tag.getBoolean("Paused");
		completed = tag.getBoolean("Completed");
		isAutoSchedule = tag.getBoolean("AutoSchedule");
		currentEntry = Math.max(0, tag.getInt("CurrentEntry"));
		if (tag.contains("Schedule"))
			schedule = Schedule.fromTag(registries, tag.getCompound("Schedule"));
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

	public ItemStack returnSchedule(HolderLookup.Provider registries) {
		if (schedule == null)
			return ItemStack.EMPTY;
		ItemStack stack = AllItems.SCHEDULE.asStack();
		schedule.savedProgress = currentEntry;
		stack.set(AllDataComponents.TRAIN_SCHEDULE, schedule.write(registries));
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
			return Component.empty();

		List<ScheduleWaitCondition> list = conditions.get(0);
		int progress = conditionProgress.get(0);
		if (progress >= list.size())
			return Component.empty();

		CompoundTag tag = conditionContext.get(0);
		ScheduleWaitCondition condition = list.get(progress);
		return condition.getWaitingStatus(level, train, tag);
	}

}
