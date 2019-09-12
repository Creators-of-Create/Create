package com.simibubi.create;

import net.minecraft.world.IWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.WorldEvent;
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
	
	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		Create.frequencyHandler.onLoadWorld(world);
		Create.constructHandler.onLoadWorld(world);
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		Create.frequencyHandler.onUnloadWorld(world);
		Create.constructHandler.onUnloadWorld(world);
	}
	
}
