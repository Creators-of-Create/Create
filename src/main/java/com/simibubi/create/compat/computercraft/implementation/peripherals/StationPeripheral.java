package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.TrainEditPacket;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.network.chat.Component;

import net.minecraftforge.network.PacketDistributor;

public class StationPeripheral extends SyncedPeripheral<StationBlockEntity> {

	public StationPeripheral(StationBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final void assemble() throws LuaException {
		if (!blockEntity.isAssembling())
			throw new LuaException("station must be in assembly mode");

		blockEntity.assemble(null);

		if (blockEntity.getStation() == null || blockEntity.getStation().getPresentTrain() == null)
			throw new LuaException("failed to assemble train");

		if (!blockEntity.exitAssemblyMode())
			throw new LuaException("failed to exit assembly mode");
	}

	@LuaFunction(mainThread = true)
	public final void disassemble() throws LuaException {
		if (blockEntity.isAssembling())
			throw new LuaException("station must not be in assembly mode");

		getTrainOrThrow();

		if (!blockEntity.enterAssemblyMode(null))
			throw new LuaException("could not disassemble train");
	}

	@LuaFunction(mainThread = true)
	public final void setAssemblyMode(boolean assemblyMode) throws LuaException {
		if (assemblyMode) {
			if (!blockEntity.enterAssemblyMode(null))
				throw new LuaException("failed to enter assembly mode");
		} else {
			if (!blockEntity.exitAssemblyMode())
				throw new LuaException("failed to exit assembly mode");
		}
	}

	@LuaFunction
	public final boolean isInAssemblyMode() {
		return blockEntity.isAssembling();
	}

	@LuaFunction
	public final String getStationName() throws LuaException {
		GlobalStation station = blockEntity.getStation();
		if (station == null)
			throw new LuaException("station is not connected to a track");

		return station.name;
	}

	@LuaFunction(mainThread = true)
	public final void setStationName(String name) throws LuaException {
		if (!blockEntity.updateName(name))
			throw new LuaException("could not set station name");
	}

	@LuaFunction
	public final boolean isTrainPresent() throws LuaException {
		GlobalStation station = blockEntity.getStation();
		if (station == null)
			throw new LuaException("station is not connected to a track");

		return station.getPresentTrain() != null;
	}

	@LuaFunction
	public final boolean isTrainImminent() throws LuaException {
		GlobalStation station = blockEntity.getStation();
		if (station == null)
			throw new LuaException("station is not connected to a track");

		return station.getImminentTrain() != null;
	}

	@LuaFunction
	public final boolean isTrainEnroute() throws LuaException {
		GlobalStation station = blockEntity.getStation();
		if (station == null)
			throw new LuaException("station is not connected to a track");

		return station.getNearestTrain() != null;
	}

	@LuaFunction
	public final String getTrainName() throws LuaException {
		Train train = getTrainOrThrow();
		return train.name.getString();
	}

	@LuaFunction(mainThread = true)
	public final void setTrainName(String name) throws LuaException {
		Train train = getTrainOrThrow();
		train.name = Component.literal(name);
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainEditPacket.TrainEditReturnPacket(train.id, name, train.icon.getId(), train.mapColorIndex));
	}

	@LuaFunction
	public final boolean hasSchedule() throws LuaException {
		Train train = getTrainOrThrow();
		return train.runtime.getSchedule() != null;
	}

	@LuaFunction
	public final CreateLuaTable getSchedule() throws LuaException {
		Train train = getTrainOrThrow();

		Schedule schedule = train.runtime.getSchedule();
		if (schedule == null)
			throw new LuaException("train doesn't have a schedule");

		return fromCompoundTag(schedule.write());
	}

	@LuaFunction(mainThread = true)
	public final void setSchedule(IArguments arguments) throws LuaException {
		if (arguments.getTable(0).size() != 2)
			throw new LuaException("Not a valid schedule");

		Object entries = arguments.getTable(0).get("entries");
		if (entries instanceof Map<?, ?> map && map.isEmpty())
			throw new LuaException("Schedule must have at least one entry");

		Train train = getTrainOrThrow();
		Schedule schedule = Schedule.fromTag(toCompoundTag(new CreateLuaTable(arguments.getTable(0))));
		boolean autoSchedule = train.runtime.getSchedule() == null || train.runtime.isAutoSchedule;
		train.runtime.setSchedule(schedule, autoSchedule);
	}

	private @NotNull Train getTrainOrThrow() throws LuaException {
		GlobalStation station = blockEntity.getStation();
		if (station == null)
			throw new LuaException("station is not connected to a track");

		Train train = station.getPresentTrain();
		if (train == null)
			throw new LuaException("there is no train present");

		return train;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Station";
	}

}
