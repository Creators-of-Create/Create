package com.simibubi.create.lib.data;

import com.simibubi.create.Create;
import com.tterrag.registrate.fabric.GatherDataEvent;
import com.tterrag.registrate.providers.RegistrateDataProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class DataInit implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		var existingData = System.getProperty("com.simibubi.create.existingData").split(";");
		var existingFileHelper = new ExistingFileHelper(Arrays.stream(existingData).map(Paths::get).toList(), Collections.emptySet(),
				true, null, null);
		Create.gatherData(generator, existingFileHelper);
		GatherDataEvent.EVENT.invoker().gatherData(generator, existingFileHelper);
	}
}
