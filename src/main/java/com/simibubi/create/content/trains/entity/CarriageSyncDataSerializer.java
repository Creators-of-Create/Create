package com.simibubi.create.content.trains.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

public class CarriageSyncDataSerializer implements EntityDataSerializer<CarriageSyncData> {

	@Override
	public void write(FriendlyByteBuf buffer, CarriageSyncData data) {
		data.write(buffer);
	}

	@Override
	public CarriageSyncData read(FriendlyByteBuf buffer) {
		CarriageSyncData data = new CarriageSyncData();
		data.read(buffer);
		return data;
	}

	@Override
	public CarriageSyncData copy(CarriageSyncData data) {
		return data.copy();
	}

}
