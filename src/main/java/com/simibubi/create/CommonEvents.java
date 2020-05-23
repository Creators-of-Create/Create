package com.simibubi.create;

import com.simibubi.create.foundation.command.CreateCommand;

import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@EventBusSubscriber
public class CommonEvents {

	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		if (event.phase == Phase.END)
			return;

		Create.tick();
	}

	@SubscribeEvent
	public static void onClose(FMLServerStoppingEvent event) {
		Create.shutdown();
	}
	
	@SubscribeEvent
	public static void serverStarting(FMLServerStartingEvent event) {
		new CreateCommand(event.getCommandDispatcher());
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onLoadWorld(world);
		Create.torquePropagator.onLoadWorld(world);
		if (event.getWorld().isRemote())
			DistExecutor.runWhenOn(Dist.CLIENT, () -> CreateClient.bufferCache::invalidate);
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onUnloadWorld(world);
		Create.torquePropagator.onUnloadWorld(world);
	}

}
