package com.simibubi.create.content.trains.schedule;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public abstract class ScheduleDataEntry implements IScheduleInput {

	protected CompoundTag data;

	public ScheduleDataEntry() {
		data = new CompoundTag();
	}

	@Override
	public CompoundTag getData() {
		return data;
	}

	@Override
	public void setData(HolderLookup.Provider registries, CompoundTag data) {
		this.data = data;
		readAdditional(registries, data);
	}

	protected void writeAdditional(HolderLookup.Provider registries, CompoundTag tag) {}

	protected void readAdditional(HolderLookup.Provider registries, CompoundTag tag) {}

	protected <T> T enumData(String key, Class<T> enumClass) {
		T[] enumConstants = enumClass.getEnumConstants();
		return enumConstants[data.getInt(key) % enumConstants.length];
	}

	protected String textData(String key) {
		return data.getString(key);
	}

	protected int intData(String key) {
		return data.getInt(key);
	}

}
