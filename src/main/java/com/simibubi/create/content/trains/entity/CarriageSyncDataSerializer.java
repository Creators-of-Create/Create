package com.simibubi.create.content.trains.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;

public class CarriageSyncDataSerializer implements EntityDataSerializer<CarriageSyncData> {

	private static final StreamCodec<RegistryFriendlyByteBuf, CarriageSyncData> STREAM_CODEC = StreamCodec.of(
			(buf, data) -> data.write(buf),
			CarriageSyncData::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, CarriageSyncData> codec() {
		return STREAM_CODEC;
	}

	@Override
	public CarriageSyncData copy(CarriageSyncData data) {
		return data.copy();
	}

}
