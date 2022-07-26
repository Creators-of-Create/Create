package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.entity.CarriageSyncDataSerializer;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class AllEntityDataSerializers {

	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	public static void register(RegisterEvent event) {
		event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, helper -> {
			helper.register(Create.asResource("carriage_data"), CARRIAGE_DATA);
		});
	}

}
