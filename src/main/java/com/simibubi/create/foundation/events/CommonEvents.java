package com.simibubi.create.foundation.events;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.simibubi.create.content.contraptions.minecart.CouplingPhysics;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileTypeManager;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.simibubi.create.content.trains.entity.CarriageEntityHandler;
import com.simibubi.create.foundation.ModFilePackResources;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.infrastructure.command.AllCommands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;

@EventBusSubscriber
public class CommonEvents {

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick();
		Create.RAILWAYS.sync.serverTick();
	}

	@SubscribeEvent
	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		CapabilityMinecartController.onChunkUnloaded(event);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		ToolboxHandler.playerLogin(player);
		Create.RAILWAYS.playerLogin(player);
	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerLoggedOutEvent event) {
		Player player = event.getEntity();
		Create.RAILWAYS.playerLogout(player);
	}

	@SubscribeEvent
	public static void onServerWorldTick(LevelTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (event.side == LogicalSide.CLIENT)
			return;
		Level world = event.level;
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
		LinkedControllerServerHandler.tick(world);
		ControlsServerHandler.tick(world);
		Create.RAILWAYS.tick(world);
	}

	@SubscribeEvent
	public static void onUpdateLivingEntity(LivingTickEvent event) {
		LivingEntity entityLiving = event.getEntity();
		Level world = entityLiving.level;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
		ToolboxHandler.entityTick(entityLiving, world);
	}

	@SubscribeEvent
	public static void onEntityAdded(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		Level world = event.getLevel();
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	@SubscribeEvent
	public static void onEntityAttackedByPlayer(AttackEntityEvent event) {
		WrenchItem.wrenchInstaKillsMinecarts(event);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		AllCommands.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onEntityEnterSection(EntityEvent.EnteringSection event) {
		CarriageEntityHandler.onEntityEnterSection(event);
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(RecipeFinder.LISTENER);
		event.addListener(PotatoProjectileTypeManager.ReloadListener.INSTANCE);
	}

	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event) {
		ServerPlayer player = event.getPlayer();
		if (player != null) {
			PotatoProjectileTypeManager.syncTo(player);
		} else {
			PotatoProjectileTypeManager.syncToAll();
		}
	}

	@SubscribeEvent
	public static void serverStopping(ServerStoppingEvent event) {
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	@SubscribeEvent
	public static void onLoadWorld(LevelEvent.Load event) {
		LevelAccessor world = event.getLevel();
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
		Create.RAILWAYS.levelLoaded(world);
	}

	@SubscribeEvent
	public static void onUnloadWorld(LevelEvent.Unload event) {
		LevelAccessor world = event.getLevel();
		Create.REDSTONE_LINK_NETWORK_HANDLER.onUnloadWorld(world);
		Create.TORQUE_PROPAGATOR.onUnloadWorld(world);
		WorldAttached.invalidateWorld(world);
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		CapabilityMinecartController.attach(event);
	}

	@SubscribeEvent
	public static void startTracking(PlayerEvent.StartTracking event) {
		CapabilityMinecartController.startTracking(event);
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}

	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {

		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			event.register(CapabilityMinecartController.class);
		}

		@SubscribeEvent
		public static void addPackFinders(AddPackFindersEvent event) {
			if (event.getPackType() == PackType.CLIENT_RESOURCES) {
				IModFileInfo modFileInfo = ModList.get().getModFileById(Create.ID);
				if (modFileInfo == null) {
					Create.LOGGER.error("Could not find Create mod file info; built-in resource packs will be missing!");
					return;
				}
				IModFile modFile = modFileInfo.getFile();
				event.addRepositorySource((consumer, constructor) -> {
					consumer.accept(Pack.create(Create.asResource("legacy_copper").toString(), false, () -> new ModFilePackResources("Create Legacy Copper", modFile, "resourcepacks/legacy_copper"), constructor, Pack.Position.TOP, PackSource.DEFAULT));
				});
			}
		}
	}
}
