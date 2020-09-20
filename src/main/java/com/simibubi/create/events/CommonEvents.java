package com.simibubi.create.events;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingHandler;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.foundation.command.AllCommands;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@EventBusSubscriber
public class CommonEvents {

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (Create.schematicReceiver == null)
			Create.schematicReceiver = new ServerSchematicLoader();
		Create.schematicReceiver.tick();
		Create.lagger.tick();
		ServerSpeedProvider.serverTick();
	}

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if (event.phase == Phase.START)
			return;
		World world = event.world;
		ContraptionCollider.runCollisions(world);
		MinecartCouplingHandler.tick(world);
	}

	@SubscribeEvent
	public static void onUpdateLivingEntity(LivingUpdateEvent event) {
		LivingEntity entityLiving = event.getEntityLiving();
		World world = entityLiving.world;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
	}

	@SubscribeEvent
	public static void onEntityAdded(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		World world = event.getWorld();
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
		MinecartCouplingHandler.handleAddedMinecart(entity, world);
	}

	@SubscribeEvent
	public static void serverStarted(FMLServerStartingEvent event) {
		AllCommands.register(event.getCommandDispatcher());
	}

	@SubscribeEvent
	public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
		event.getServer()
			.getResourceManager()
			.addReloadListener(RecipeFinder.LISTENER);
	}

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppingEvent event) {
		Create.schematicReceiver.shutdown();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onLoadWorld(world);
		Create.torquePropagator.onLoadWorld(world);
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onUnloadWorld(world);
		Create.torquePropagator.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
	}

}
