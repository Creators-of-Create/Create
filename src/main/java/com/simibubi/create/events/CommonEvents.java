package com.simibubi.create.events;

import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.fluids.recipe.FluidTransferRecipes;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.wrench.WrenchItem;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandler;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;
import com.simibubi.create.content.curiosities.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.logistics.item.LinkedControllerServerHandler;
import com.simibubi.create.foundation.command.AllCommands;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.world.BlockEvent.FluidPlaceBlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

@EventBusSubscriber
public class CommonEvents {

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		Create.SCHEMATIC_RECEIVER.tick();
		Create.LAGGER.tick();
		ServerSpeedProvider.serverTick();
	}

	@SubscribeEvent
	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		CapabilityMinecartController.onChunkUnloaded(event);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		ToolboxHandler.playerLogin(player);
	}

	@SubscribeEvent
	public static void whenFluidsMeet(FluidPlaceBlockEvent event) {
		BlockState blockState = event.getOriginalState();
		FluidState fluidState = blockState.getFluidState();
		BlockPos pos = event.getPos();
		LevelAccessor world = event.getWorld();

		if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
			return;

		for (Direction direction : Iterate.directions) {
			FluidState metFluidState =
				fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
			if (!metFluidState.is(FluidTags.WATER))
				continue;
			BlockState lavaInteraction = AllFluids.getLavaInteraction(metFluidState);
			if (lavaInteraction == null)
				continue;
			event.setNewState(lavaInteraction);
			break;
		}
	}

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if (event.phase == Phase.START)
			return;
		Level world = event.world;
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
		LinkedControllerServerHandler.tick(world);
	}

	@SubscribeEvent
	public static void onUpdateLivingEntity(LivingUpdateEvent event) {
		LivingEntity entityLiving = event.getEntityLiving();
		Level world = entityLiving.level;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
		ToolboxHandler.entityTick(entityLiving, world);
	}

	@SubscribeEvent
	public static void onEntityAdded(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		Level world = event.getWorld();
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
	public static void registerReloadListeners(AddReloadListenerEvent event) {
		event.addListener(RecipeFinder.LISTENER);
		event.addListener(PotionMixingRecipeManager.LISTENER);
		event.addListener(FluidTransferRecipes.LISTENER);
		event.addListener(PotatoProjectileTypeManager.ReloadListener.INSTANCE);
	}

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppingEvent event) {
		Create.SCHEMATIC_RECEIVER.shutdown();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		LevelAccessor world = event.getWorld();
		Create.REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(world);
		Create.TORQUE_PROPAGATOR.onLoadWorld(world);
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		LevelAccessor world = event.getWorld();
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

}
