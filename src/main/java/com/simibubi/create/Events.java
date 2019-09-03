package com.simibubi.create;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@EventBusSubscriber
public class Events {

	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		
		Create.tick();
	}
		
	@SubscribeEvent
	public static void onClose(FMLServerStoppingEvent event) {
		Create.shutdown();
	}
	
	
}
