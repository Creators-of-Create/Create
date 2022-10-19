package com.simibubi.create.compat.computercraft.peripherals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.compat.computercraft.CreateLuaTable;
import com.simibubi.create.compat.computercraft.peripherals.PeripheralBase;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleEntry;
import com.simibubi.create.content.logistics.trains.management.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.logistics.trains.management.schedule.destination.ScheduleInstruction;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.StringHelper;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class StationPeripheral extends PeripheralBase<StationTileEntity> {

	public StationPeripheral(StationTileEntity tile) {
		super(tile);
	}

	@LuaFunction(mainThread = true)
	public void setSchedule(IArguments arguments) throws LuaException {
		GlobalStation station = tile.getStation();
		if (station == null)
			throw new LuaException("train station does not exist");

		Train train = station.getPresentTrain();
		if (train == null)
			throw new LuaException("there is no train present");

		Schedule schedule = parseSchedule(arguments);
		train.runtime.setSchedule(schedule, true);
	}

	private static Schedule parseSchedule(IArguments arguments) throws LuaException {
		CreateLuaTable scheduleTable = new CreateLuaTable(arguments.getTable(0));
		Schedule schedule = new Schedule();

		schedule.cyclic = scheduleTable.getOptBoolean("cyclic").orElse(true);
		CreateLuaTable entriesTable = scheduleTable.getTable("entries");

		for (CreateLuaTable entryTable : entriesTable.tableValues()) {
			ScheduleEntry entry = new ScheduleEntry();

			entry.instruction = getInstruction(entryTable);

			// Add conditions
			if (entry.instruction.supportsConditions()) {
				for (CreateLuaTable conditionsListTable : entryTable.getTable("conditions").tableValues()) {
					List<ScheduleWaitCondition> conditionsList = new ArrayList<>();

					for (CreateLuaTable conditionTable : conditionsListTable.tableValues()) {
						conditionsList.add(getCondition(conditionTable));
					}

					entry.conditions.add(conditionsList);
				}
			}

			schedule.entries.add(entry);
		}

		return schedule;
	}

	private static ScheduleInstruction getInstruction(CreateLuaTable entry) throws LuaException {
		ResourceLocation location = new ResourceLocation(entry.getString("instruction"));

		for (Pair<ResourceLocation, Supplier<? extends ScheduleInstruction>> pair : Schedule.INSTRUCTION_TYPES)
			if (pair.getFirst().equals(location)) {
				ScheduleInstruction instruction = pair.getSecond().get();
				instruction.setData(getEntryData(entry.getTable("data")));

				return instruction;
			}

		throw new LuaException("instruction " + location + " is not a valid instruction type");
	}

	private static ScheduleWaitCondition getCondition(CreateLuaTable entry) throws LuaException {
		ResourceLocation location = new ResourceLocation(entry.getString("condition"));

		for (Pair<ResourceLocation, Supplier<? extends ScheduleWaitCondition>> pair : Schedule.CONDITION_TYPES)
			if (pair.getFirst().equals(location)) {
				ScheduleWaitCondition condition = pair.getSecond().get();
				condition.setData(getEntryData(entry.getTable("data")));

				return condition;
			}

		throw new LuaException("condition " + location + " is not a valid condition type");
	}

	private static CompoundTag getEntryData(CreateLuaTable data) throws LuaException {
		CompoundTag tag = new CompoundTag();

		for (String key : data.stringKeySet()) {
			String tagKey = StringHelper.snakeCaseToCamelCase(key);
			Object value = data.get(key);

			if (value instanceof Boolean)
				tag.putBoolean(tagKey, (Boolean) value);
			else if (value instanceof Number)
				tag.putDouble(tagKey, ((Number) value).doubleValue());
			else if (value instanceof String)
				tag.putString(tagKey, (String) value);
			else
				throw new LuaException("");
		}

		return tag;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Station";
	}

}
