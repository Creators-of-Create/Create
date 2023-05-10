package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.girder.GirderWrenchBehavior;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlockItem;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.content.logistics.trains.CameraDistanceModifier;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.logistics.trains.entity.TrainRelocator;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingClient;
import com.simibubi.create.content.logistics.trains.management.schedule.TrainHatArmorLayer;
import com.simibubi.create.content.logistics.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;
		if (event.phase == Phase.START) {
			LinkedControllerClientHandler.tick();
			ControlsHandler.tick();
			AirCurrent.tickClientPlayerSounds();
			return;
		}

		SoundScapes.tick();
		AnimationTickHolder.tick();
		ScrollValueHandler.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.GLUE_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);
		CreateClient.RAILWAYS.clientTick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		PonderTooltipHandler.tick();
		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		GirderWrenchBehavior.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		PlacementHelpers.tick();
		CreateClient.OUTLINER.tickOutlines();
		CreateClient.GHOST_BLOCKS.tickGhosts();
		ContraptionRenderDispatcher.tick(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		TrainRelocator.clientTick();
		DisplayLinkBlockItem.clientTick();
		CurvedTrackInteraction.clientTick();
		CameraDistanceModifier.tick();
		CameraAngleAnimationService.tick();
		TrainHUD.tick();
	}

	@SubscribeEvent
	public static void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {
		CreateClient.checkGraphicsFanciness();
	}

	@SubscribeEvent
	public static void onLeave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		CreateClient.RAILWAYS.cleanUp();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		LevelAccessor world = event.getWorld();
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		if (!event.getWorld()
			.isClientSide())
			return;
		CreateClient.invalidateRenderers();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
		AnimationTickHolder.reset();
		ControlsHandler.levelUnloaded(event.getWorld());
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderLevelLastEvent event) {
		PoseStack ms = event.getPoseStack();
		ms.pushPose();
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();

		TrackBlockOutline.drawCurveSelection(ms, buffer, camera);
		TrackTargetingClient.render(ms, buffer, camera);
		CouplingRenderer.renderAll(ms, buffer, camera);
		CarriageCouplingRenderer.renderAll(ms, buffer, camera);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer, camera);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer, camera);
		CreateClient.OUTLINER.renderOutlines(ms, buffer, camera, partialTicks);

		buffer.draw();
		RenderSystem.enableCull();
		ms.popPose();
	}

	@SubscribeEvent
	public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		if (CameraAngleAnimationService.isYawAnimating())
			event.setYaw(CameraAngleAnimationService.getYaw(partialTicks));

		if (CameraAngleAnimationService.isPitchAnimating())
			event.setPitch(CameraAngleAnimationService.getPitch(partialTicks));
	}

	@SubscribeEvent
	public static void getItemTooltipColor(RenderTooltipEvent.Color event) {
		PonderTooltipHandler.handleTooltipColor(event);
	}

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (event.getPlayer() == null)
			return;

		ItemStack stack = event.getItemStack();
		String translationKey = stack.getItem()
			.getDescriptionId(stack);

		if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX))
			if (TooltipHelper.hasTooltip(stack, event.getPlayer())) {
				List<Component> itemTooltip = event.getToolTip();
				List<Component> toolTip = new ArrayList<>();
				toolTip.add(itemTooltip.remove(0));
				TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
				itemTooltip.addAll(0, toolTip);
			}

		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof SteamEngineBlock) {
				List<Component> kineticStats = ItemDescription.getKineticStats(item.getBlock());
				if (!kineticStats.isEmpty()) {
					event.getToolTip()
						.add(Components.immutableEmpty());
					event.getToolTip()
						.addAll(kineticStats);
				}
			}
		}

		PonderTooltipHandler.addToTooltip(event.getToolTip(), stack);
		SequencedAssemblyRecipe.addToTooltip(event.getToolTip(), stack);
	}

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	@SubscribeEvent
	public static void onMount(EntityMountEvent event) {
		if (event.getEntityMounting() != Minecraft.getInstance().player)
			return;

		if (event.isDismounting()) {
			CameraDistanceModifier.reset();
			return;
		}

		if (!event.isMounting() || !(event.getEntityBeingMounted() instanceof CarriageContraptionEntity carriage)) {
			return;
		}

		CameraDistanceModifier.zoomOut();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	@SubscribeEvent
	public static void getFogDensity(EntityViewRenderEvent.RenderFogEvent event) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			event.scaleFarPlaneDistance(1f / 32f * AllConfigs.CLIENT.chocolateTransparencyMultiplier.getF());
			event.setCanceled(true);
			return;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			event.scaleFarPlaneDistance(1f / 8f * AllConfigs.CLIENT.honeyTransparencyMultiplier.getF());
			event.setCanceled(true);
			return;
		}

		if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
			.isWornBy(Minecraft.getInstance().cameraEntity)) {
			event.scaleFarPlaneDistance(6.25f);
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void getFogColor(EntityViewRenderEvent.FogColors event) {
		Camera info = event.getCamera();
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
			return;

		Fluid fluid = fluidState.getType();

		if (AllFluids.CHOCOLATE.get()
			.isSame(fluid)) {
			event.setRed(98 / 255f);
			event.setGreen(32 / 255f);
			event.setBlue(32 / 255f);
			return;
		}

		if (AllFluids.HONEY.get()
			.isSame(fluid)) {
			event.setRed(234 / 255f);
			event.setGreen(174 / 255f);
			event.setBlue(47 / 255f);
			return;
		}
	}

	@SubscribeEvent
	public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {

		@SubscribeEvent
		public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
		}

		@SubscribeEvent
		public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
			EntityRenderDispatcher dispatcher = Minecraft.getInstance()
				.getEntityRenderDispatcher();
			CopperBacktankArmorLayer.registerOnAll(dispatcher);
			TrainHatArmorLayer.registerOnAll(dispatcher);
		}

		@SubscribeEvent
		public static void onLoadComplete(FMLLoadCompleteEvent event) {
			ModContainer createContainer = ModList.get()
				.getModContainerById(Create.ID)
				.orElseThrow(() -> new IllegalStateException("Create mod container missing on LoadComplete"));
			createContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
				() -> new ConfigGuiHandler.ConfigGuiFactory(
					(mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen)));
		}

	}

}
