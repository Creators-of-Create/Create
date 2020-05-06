package com.simibubi.create.foundation.utility.data;

import com.simibubi.create.data.CreateAdvancements;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		gen.addProvider(new AllBlocksTagProvider(gen));
		gen.addProvider(new AllItemsTagProvider(gen));
		gen.addProvider(new CreateAdvancements(gen));
	}

}
