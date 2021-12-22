package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.symmetry.SymmetryHandler;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ui.OpenCreateMenuButton;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.lib.event.ClientWorldEvents;
import com.simibubi.create.lib.event.FogEvents;
import com.simibubi.create.lib.event.LeftClickAirCallback;
import com.simibubi.create.lib.event.OnTextureStitchCallback;
import com.simibubi.create.lib.event.OverlayRenderCallback;

import com.simibubi.create.lib.event.ParticleManagerRegistrationCallback;
import com.simibubi.create.lib.event.PlayerTickEndCallback;

import com.simibubi.create.lib.event.RenderHandCallback;

import com.simibubi.create.lib.event.RenderTickStartCallback;
import com.simibubi.create.lib.event.RenderTooltipBorderColorCallback;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ClientEvents {

	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	public static void onTickStart(Minecraft client) {
		LinkedControllerClientHandler.tick();
		AirCurrent.tickClientPlayerSounds();
	}

	public static void onTick(Minecraft client) {
		if (!isGameActive())
			return;

		Level world = Minecraft.getInstance().level;

		SoundScapes.tick();
		AnimationTickHolder.tick();
		ScrollValueHandler.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);

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
	}

	public static void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		CreateClient.checkGraphicsFanciness();
	}

	public static void onLoadWorld(Minecraft client, ClientLevel world) {
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	public static void onUnloadWorld(Minecraft client, ClientLevel world) {
		if (world
			.isClientSide()) {
			CreateClient.invalidateRenderers();
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
			AnimationTickHolder.reset();
		}
	}

	public static void onRenderWorld(WorldRenderContext event) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		float pt = AnimationTickHolder.getPartialTicks();

		PoseStack ms = event.matrixStack();
		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		CouplingRenderer.renderAll(ms, buffer);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer);

		CreateClient.OUTLINER.renderOutlines(ms, buffer, pt);
		// LightVolumeDebugger.render(ms, buffer);
		buffer.draw();
		RenderSystem.enableCull();

		ms.popPose();
	}

	public static RenderTooltipBorderColorCallback.BorderColorEntry getItemTooltipColor(ItemStack stack, int originalBorderColorStart, int originalBorderColorEnd) {
		return PonderTooltipHandler.handleTooltipColor(stack, originalBorderColorStart, originalBorderColorEnd);
	}

	public static void addToItemTooltip(ItemStack stack, TooltipFlag iTooltipFlag, List<Component> itemTooltip) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (Minecraft.getInstance().player == null)
			return;

		String translationKey = stack.getItem()
			.getDescriptionId(stack);

		if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX))
			if (TooltipHelper.hasTooltip(stack, Minecraft.getInstance().player)) {
				List<Component> toolTip = new ArrayList<>();
				toolTip.add(itemTooltip.remove(0));
				TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
				itemTooltip.addAll(0, toolTip);
			}

		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof EngineBlock) {
				List<Component> kineticStats = ItemDescription.getKineticStats(item.getBlock());
				if (!kineticStats.isEmpty()) {
					itemTooltip
						.add(new TextComponent(""));
					itemTooltip
						.addAll(kineticStats);
				}
			}
		}

		PonderTooltipHandler.addToTooltip(itemTooltip, stack);
		SequencedAssemblyRecipe.addToTooltip(itemTooltip, stack);
	}

	public static void onRenderTick() {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static float getFogDensity(Camera info, float currentDensity) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidstate = level.getFluidState(blockPos);
        if (info.getPosition().y > blockPos.getY() + fluidstate.getHeight(level, blockPos))
           return currentDensity;

		Fluid fluid = fluidstate.getType();

		if (AllFluids.CHOCOLATE.get().isSame(fluid)) {
//			event.setDensity(5f);
//			event.setCanceled(true);
			return 5f;
		}

		if (AllFluids.HONEY.get().isSame(fluid)) {
//			event.setDensity(1.5f);
//			event.setCanceled(true);
			return 1.5f;
		}

		if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
			.isWornBy(Minecraft.getInstance().cameraEntity)) {
//			event.setDensity(300f);
//			event.setCanceled(true);
			return 300f;
		}
		return currentDensity;
	}

	public static Vector3f getFogColor(Camera info, Vector3f currentColor) {
		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidstate = level.getFluidState(blockPos);
        if (info.getPosition().y > blockPos.getY() + fluidstate.getHeight(level, blockPos))
           return currentColor;

		Fluid fluid = fluidstate.getType();

		if (AllFluids.CHOCOLATE.get().isSame(fluid)) {
//			event.setRed(98 / 256f);
//			event.setGreen(32 / 256f);
//			event.setBlue(32 / 256f);
			return new Vector3f(98 / 256f, 32 / 256f, 32 / 256f);
		}

		if (AllFluids.HONEY.get().isSame(fluid)) {
//			event.setRed(234 / 256f);
//			event.setGreen(174 / 256f);
//			event.setBlue(47 / 256f);
			return new Vector3f(234 / 256f, 174 / 256f, 47 / 256f);
		}
		return currentColor;
	}

	public static void leftClickEmpty(LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

	public static class ModBusEvents {

		public static void registerClientReloadListeners() {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			if (resourceManager instanceof ReloadableResourceManager reloadable)
				reloadable.registerReloadListener(CreateClient.RESOURCE_RELOAD_LISTENER);
		}

		public static void addEntityRendererLayers() {
			EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			CopperBacktankArmorLayer.registerOnAll(dispatcher);
		}

//		@SubscribeEvent
//		public static void loadCompleted(FMLLoadCompleteEvent event) {
//			ModContainer createContainer = ModList.get()
//				.getModContainerById(Create.ID)
//				.orElseThrow(() -> new IllegalStateException("Create Mod Container missing after loadCompleted"));
//			createContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
//				() -> new ConfigGuiHandler.ConfigGuiFactory((mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen)));
//		}

	}

	public static void register() {
		ModBusEvents.registerClientReloadListeners();
		ParticleManagerRegistrationCallback.EVENT.register(() -> {
			AllParticleTypes.registerFactories();
			ModBusEvents.addEntityRendererLayers();
		});

		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onTickStart);
		RenderTickStartCallback.EVENT.register(ClientEvents::onRenderTick);
		ClientPlayConnectionEvents.JOIN.register(ClientEvents::onJoin);
		ClientWorldEvents.LOAD.register(ClientEvents::onLoadWorld);
		ClientWorldEvents.UNLOAD.register(ClientEvents::onUnloadWorld);
		WorldRenderEvents.END.register(ClientEvents::onRenderWorld);
		ItemTooltipCallback.EVENT.register(ClientEvents::addToItemTooltip);
		RenderTooltipBorderColorCallback.EVENT.register(ClientEvents::getItemTooltipColor);
		LeftClickAirCallback.EVENT.register(ClientEvents::leftClickEmpty);
		FogEvents.SET_DENSITY.register(ClientEvents::getFogDensity);
		FogEvents.SET_COLOR.register(ClientEvents::getFogColor);

		ClientChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnloaded);
		ClientTickEvents.END_WORLD_TICK.register(CommonEvents::onWorldTick);
		ClientEntityEvents.ENTITY_LOAD.register(CommonEvents::onEntityAdded);
		ClientWorldEvents.LOAD.register((client, world) -> CommonEvents.onLoadWorld(world));
		ClientWorldEvents.UNLOAD.register((client, world) -> CommonEvents.onUnloadWorld(world));

		// External Events

		RenderHandCallback.EVENT.register(ExtendoGripRenderHandler::onRenderPlayerHand);
		UseBlockCallback.EVENT.register(ItemUseOverrides::onBlockActivated);
		UseBlockCallback.EVENT.register(EdgeInteractionHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(FilteringHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(LinkHandler::onBlockActivated);
		UseBlockCallback.EVENT.register(ArmInteractionPointHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(EjectorTargetHandler::rightClickingBlocksSelectsThem);
		UseBlockCallback.EVENT.register(FurnaceEngineBlock::usingFurnaceEngineOnFurnacePreventsGUI);
		AttackBlockCallback.EVENT.register(ArmInteractionPointHandler::leftClickingBlocksDeselectsThem);
		AttackBlockCallback.EVENT.register(EjectorTargetHandler::leftClickingBlocksDeselectsThem);
		WorldRenderEvents.END.register(SymmetryHandler::render);
		ClientTickEvents.END_CLIENT_TICK.register(SymmetryHandler::onClientTick);
		PlayerBlockBreakEvents.AFTER.register(SymmetryHandler::onBlockDestroyed);
		PlayerTickEndCallback.EVENT.register(ContraptionHandlerClient::preventRemotePlayersWalkingAnimations);
		UseBlockCallback.EVENT.register(ContraptionHandlerClient::rightClickingOnContraptionsGetsHandledLocally);
		OverlayRenderCallback.EVENT.register(PlacementHelpers::afterRenderOverlayLayer);
//		OnTextureStitchCallback.PRE.register(SpriteShifter::onTextureStitchPre);
//		OnTextureStitchCallback.POST.register(SpriteShifter::onTextureStitchPost);
		ScreenEvents.AFTER_INIT.register(OpenCreateMenuButton.OpenConfigButtonHandler::onGuiInit);

		// Flywheel Events

		FlywheelEvents.BEGIN_FRAME.register(ContraptionRenderDispatcher::beginFrame);
		FlywheelEvents.RENDER_LAYER.register(ContraptionRenderDispatcher::renderLayer);
		FlywheelEvents.RELOAD_RENDERERS.register(ContraptionRenderDispatcher::onRendererReload);
		FlywheelEvents.GATHER_CONTEXT.register(ContraptionRenderDispatcher::gatherContext);
	}

}
