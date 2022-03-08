package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.entity.CarriageSyncDataSerializer;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class AllEntityDataSerializers {

	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	public static void register(RegistryEvent.Register<DataSerializerEntry> event) {
		IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
		registry.register(new DataSerializerEntry(CARRIAGE_DATA).setRegistryName(Create.asResource("carriage_data")));
	}

}
