package com.simibubi.create.content.trains.schedule;

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
	public void setData(CompoundTag data) {
		this.data = data;
		readAdditional(data);
	}

	protected void writeAdditional(CompoundTag tag) {};

	protected void readAdditional(CompoundTag tag) {};

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
