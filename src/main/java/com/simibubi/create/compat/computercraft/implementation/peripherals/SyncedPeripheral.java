package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.compat.computercraft.implementation.ComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.StringHelper;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
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

public abstract class SyncedPeripheral<T extends SmartBlockEntity> implements IPeripheral {

	protected final T blockEntity;
	private final Set<IComputerAccess> computerAccesses = ConcurrentHashMap.newKeySet();

	public SyncedPeripheral(T blockEntity) {
		this.blockEntity = blockEntity;
	}

	@Override
	public void attach(@NotNull IComputerAccess computer) {
		computerAccesses.add(computer);
		updateBlockEntity();
	}

	@Override
	public void detach(@NotNull IComputerAccess computer) {
		computerAccesses.remove(computer);
		updateBlockEntity();
	}

	private void updateBlockEntity() {
		boolean hasAttachedComputer = computerAccesses.size() > 0;

		blockEntity.getBehaviour(ComputerBehaviour.TYPE).setHasAttachedComputer(hasAttachedComputer);
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new AttachedComputerPacket(blockEntity.getBlockPos(), hasAttachedComputer));
	}
  
	public void sendEvent(String eventName, Object... args) {
		for (IComputerAccess computer : computerAccesses) {
			computer.queueEvent(eventName, args);
		}
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

	protected static @NotNull CreateLuaTable fromCompoundTag(CompoundTag tag) throws LuaException {
		return (CreateLuaTable) fromNBTTag(null, tag);
	}

	protected static @NotNull Object fromNBTTag(@Nullable String key, Tag tag) throws LuaException {
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

	protected static @NotNull CompoundTag toCompoundTag(CreateLuaTable table) throws LuaException {
		return (CompoundTag) toNBTTag(null, table.getMap());
	}

	protected static @NotNull Tag toNBTTag(@Nullable String key, Object value) throws LuaException {
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
				if (v.get(i) != null)
					list.add(toNBTTag(null, v.get(i)));
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

}
