package com.simibubi.create;

import com.simibubi.create.content.trains.entity.CarriageSyncDataSerializer;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AllEntityDataSerializers {
	private static final DeferredRegister<DataSerializerEntry> REGISTER = DeferredRegister.create(ForgeRegistries.Keys.DATA_SERIALIZERS, Create.ID);

	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	public static final RegistryObject<DataSerializerEntry> CARRIAGE_DATA_ENTRY = REGISTER.register("carriage_data", () -> new DataSerializerEntry(CARRIAGE_DATA));

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
