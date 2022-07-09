package com.simibubi.create;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueSelectionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.render.SBBContraptionManager;
import com.simibubi.create.content.contraptions.goggles.GoggleOverlayRenderer;
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
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.ModelSwapper;
import com.simibubi.create.foundation.utility.ShippedResourcePacks;
import com.simibubi.create.foundation.utility.ghost.GhostBlocks;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static final SuperByteBufferCache BUFFER_CACHE = new SuperByteBufferCache();
	public static final Outliner OUTLINER = new Outliner();
	public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();
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
		BUFFER_CACHE.registerCompartment(CachedBufferer.GENERIC_TILE);
		BUFFER_CACHE.registerCompartment(CachedBufferer.PARTIAL);
		BUFFER_CACHE.registerCompartment(CachedBufferer.DIRECTIONAL_PARTIAL);
		BUFFER_CACHE.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		BUFFER_CACHE.registerCompartment(SBBContraptionManager.CONTRAPTION, 20);
		BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		ShippedResourcePacks.extractFiles("Copper Legacy Pack");

		AllKeys.register();
		// AllFluids.assignRenderLayers();
		AllBlockPartials.init();
		AllStitchedTextures.init();

		PonderIndex.register();
		PonderIndex.registerTags();

		registerOverlays();

		UIRenderHelper.init();
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

	public static void invalidateRenderers() {
		BUFFER_CACHE.invalidate();

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

		MutableComponent text = ComponentUtils.wrapInSquareBrackets(new TextComponent("WARN"))
			.withStyle(ChatFormatting.GOLD)
			.append(new TextComponent(
				" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.withStyle(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new TextComponent("Click here to disable this warning"))));

		mc.gui.handleChat(ChatType.CHAT, text, mc.player.getUUID());
	}

}
