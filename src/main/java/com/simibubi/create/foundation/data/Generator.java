package com.simibubi.create.foundation.data;

import com.simibubi.create.foundation.advancement.AllAdvancements;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		gen.addProvider(new AllItemsTagProvider(gen));
		gen.addProvider(new AllAdvancements(gen));
	}

}
