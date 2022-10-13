package com.simibubi.create;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueSelectionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.render.SBBContraptionManager;
import com.simibubi.create.content.contraptions.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.bell.SoulPulseEffectHandler;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.foundation.ClientResourceReloadListener;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.CreateMainMenuScreen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.render.CachedPartialBuffers;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.FlwSuperBufferFactory;
import com.simibubi.create.foundation.utility.ModelSwapper;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static final ModelSwapper MODEL_SWAPPER = new ModelSwapper();
	public static final CasingConnectivity CASING_CONNECTIVITY = new CasingConnectivity();

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
	public static final SuperGlueSelectionHandler GLUE_HANDLER = new SuperGlueSelectionHandler();

	public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
	public static final PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
	public static final SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(CreateClient::clientInit);
		modEventBus.addListener(AllParticleTypes::registerFactories);
		modEventBus.addListener(CreateContexts::flwInit);
		modEventBus.addListener(ContraptionRenderDispatcher::gatherContext);

		MODEL_SWAPPER.registerListeners(modEventBus);

		ZAPPER_RENDER_HANDLER.registerListeners(forgeEventBus);
		POTATO_CANNON_RENDER_HANDLER.registerListeners(forgeEventBus);
	}

	public static void clientInit(final FMLClientSetupEvent event) {
		SuperBufferFactory.setInstance(new FlwSuperBufferFactory());

		SuperByteBufferCache.getInstance().registerCompartment(CachedPartialBuffers.PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(CachedPartialBuffers.DIRECTIONAL_PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		SuperByteBufferCache.getInstance().registerCompartment(SBBContraptionManager.CONTRAPTION, 20);

		AllKeys.register();
		AllBlockPartials.init();
		AllStitchedTextures.init();

		PonderIndex.addPlugin(new CreatePonderPlugin());

		setupConfigUIBackground();

		registerOverlays();
	}

	private static void registerOverlays() {
		// Register overlays in reverse order
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.AIR_LEVEL_ELEMENT, "Create's Remaining Air", CopperBacktankArmorLayer.REMAINING_AIR_OVERLAY);
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "Create's Train Driver HUD", TrainHUD.OVERLAY);
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Goggle Information", GoggleOverlayRenderer.OVERLAY);
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Blueprints", BlueprintOverlayRenderer.OVERLAY);
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Linked Controller", LinkedControllerClientHandler.OVERLAY);
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Schematics", SCHEMATIC_HANDLER.getOverlayRenderer());
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Create's Toolboxes", ToolboxHandlerClient.OVERLAY);
	}

	private static void setupConfigUIBackground() {
		ConfigScreen.backgrounds.put(Create.ID, (screen, ms, partialTicks) -> {
			CreateMainMenuScreen.PANORAMA.render(screen.getMinecraft().getDeltaFrameTime(), 1);

			RenderSystem.setShaderTexture(0, CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			Screen.blit(ms, 0, 0, screen.width, screen.height, 0.0F, 0.0F, 16, 128, 16, 128);

			Screen.fill(ms, 0, 0, screen.width, screen.height, 0x90_282c34);
		});

		ConfigScreen.shadowState = AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y);

		BaseConfigScreen.setDefaultActionFor(Create.ID, base -> base
				.withTitles("Client Settings", "World Generation Settings", "Gameplay Settings")
				.withSpecs(AllConfigs.CLIENT.specification, AllConfigs.COMMON.specification, AllConfigs.SERVER.specification)
		);
	}

	public static void invalidateRenderers() {
		SCHEMATIC_HANDLER.updateRenderers();
		ContraptionRenderDispatcher.reset();
	}

	public static void checkGraphicsFanciness() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		if (mc.options.graphicsMode != GraphicsStatus.FABULOUS)
			return;

		if (AllConfigs.CLIENT.ignoreFabulousWarning.get())
			return;

		MutableComponent text = ComponentUtils.wrapInSquareBrackets(Components.literal("WARN"))
			.withStyle(ChatFormatting.GOLD)
			.append(Components.literal(
				" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.withStyle(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Components.literal("Click here to disable this warning"))));

		mc.player.displayClientMessage(text, false);
	}

}
