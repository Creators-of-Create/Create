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
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.StringHelper;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
		train.name = Components.literal(name);
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainEditPacket.TrainEditReturnPacket(train.id, name, train.icon.getId()));
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

		if (arguments.getTable(0).size() != 2) {
			throw new LuaException("Not a valid schedule");
		}

		var entries = arguments.getTable(0).get("entries");
		if (entries instanceof Map<?, ?> map && map.isEmpty()) {
			throw new LuaException("Schedule must have at least one entry");
		}

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

	private static @NotNull CreateLuaTable fromCompoundTag(CompoundTag tag) throws LuaException {
		return (CreateLuaTable) fromNBTTag(null, tag);
	}

	private static @NotNull Object fromNBTTag(@Nullable String key, Tag tag) throws LuaException {
		byte type = tag.getId();

		if (type == Tag.TAG_BYTE && key != null && key.equals("Count"))
			return ((NumericTag) tag).getAsByte();
		else if (type == Tag.TAG_BYTE)
			return ((NumericTag) tag).getAsByte() != 0;
		else if (type == Tag.TAG_SHORT || type == Tag.TAG_INT || type == Tag.TAG_LONG)
			return ((NumericTag) tag).getAsLong();
		else if (type == Tag.TAG_FLOAT || type == Tag.TAG_DOUBLE)
			return ((NumericTag) tag).getAsDouble();
		else if (type == Tag.TAG_STRING)
			return tag.getAsString();
		else if (type == Tag.TAG_LIST || type == Tag.TAG_BYTE_ARRAY || type == Tag.TAG_INT_ARRAY || type == Tag.TAG_LONG_ARRAY) {
			CreateLuaTable list = new CreateLuaTable();
			CollectionTag<?> listTag = (CollectionTag<?>) tag;

			for (int i = 0; i < listTag.size(); i++) {
				list.put(i + 1, fromNBTTag(null, listTag.get(i)));
			}

			return list;

		} else if (type == Tag.TAG_COMPOUND) {
			CreateLuaTable table = new CreateLuaTable();
			CompoundTag compoundTag = (CompoundTag) tag;

			for (String compoundKey : compoundTag.getAllKeys()) {
				table.put(
						StringHelper.camelCaseToSnakeCase(compoundKey),
						fromNBTTag(compoundKey, compoundTag.get(compoundKey))
				);
			}

			return table;
		}

		throw new LuaException("unknown tag type " + tag.getType().getName());
	}

	private static @NotNull CompoundTag toCompoundTag(CreateLuaTable table) throws LuaException {
		return (CompoundTag) toNBTTag(null, table.getMap());
	}

	private static @NotNull Tag toNBTTag(@Nullable String key, Object value) throws LuaException {
		if (value instanceof Boolean v)
			return ByteTag.valueOf(v);
		else if (value instanceof Byte || (key != null && key.equals("count")))
			return ByteTag.valueOf(((Number) value).byteValue());
		else if (value instanceof Number v) {
			// If number is numerical integer
			if (v.intValue() == v.doubleValue())
				return IntTag.valueOf(v.intValue());
			else
				return DoubleTag.valueOf(v.doubleValue());

		} else if (value instanceof String v)
			return StringTag.valueOf(v);
		else if (value instanceof Map<?, ?> v && v.containsKey(1.0)) { // List
			ListTag list = new ListTag();
			
			for (double i = 1; i <= v.size(); i++) {
				var entry = v.get(i);
				if (entry != null) {
					list.add(toNBTTag(null, v.get(i)));
				}
			}

			return list;

		} else if (value instanceof Map<?, ?> v) { // Table/Map
			CompoundTag compound = new CompoundTag();
			for (Object objectKey : v.keySet()) {
				if (!(objectKey instanceof String compoundKey))
					throw new LuaException("table key is not of type string");

				compound.put(
						// Items serialize their resource location as "id" and not as "Id".
						// This check is needed to see if the 'i' should be left lowercase or not.
						// Items store "count" in the same compound tag, so we can check for its presence to see if this is a serialized item
						compoundKey.equals("id") && v.containsKey("count") ? "id" : StringHelper.snakeCaseToCamelCase(compoundKey),
						toNBTTag(compoundKey, v.get(compoundKey))
				);
			}

			return compound;
		}

		throw new LuaException("unknown object type " + value.getClass().getName());
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Station";
	}

}
