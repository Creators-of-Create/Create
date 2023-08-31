package com.simibubi.create;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.render.SBBContraptionManager;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.content.equipment.bell.SoulPulseEffectHandler;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonRenderHandler;
import com.simibubi.create.content.equipment.zapper.ZapperRenderHandler;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.content.schematics.client.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.foundation.ClientResourceReloadListener;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsClient;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.ModelSwapper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.gui.CreateMainMenuScreen;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
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
	public static final ValueSettingsClient VALUE_SETTINGS_HANDLER = new ValueSettingsClient();

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
		//BUFFER_CACHE.registerCompartment(CachedBufferer.GENERIC_BLOCK);
		//BUFFER_CACHE.registerCompartment(CachedPartialBuffers.partial);
		//BUFFER_CACHE.registerCompartment(CachedBufferer.DIRECTIONAL_PARTIAL);
		//BUFFER_CACHE.registerCompartment(KineticBlockEntityRenderer.KINETIC_BLOCK);
		//BUFFER_CACHE.registerCompartment(WaterWheelRenderer.WATER_WHEEL);
		//BUFFER_CACHE.registerCompartment(SBBContraptionManager.CONTRAPTION, 20);
		//BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.DIRECTIONAL_PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(KineticBlockEntityRenderer.KINETIC_BLOCK);
		SuperByteBufferCache.getInstance().registerCompartment(WaterWheelRenderer.WATER_WHEEL);
		SuperByteBufferCache.getInstance().registerCompartment(SBBContraptionManager.CONTRAPTION, 20);

		AllPartialModels.init();


		//AllPonderTags.register();
		//PonderIndex.register();
		PonderIndex.addPlugin(new CreatePonderPlugin());

		setupConfigUIBackground();
	}

	private static void setupConfigUIBackground() {
		ConfigScreen.backgrounds.put(Create.ID, (screen, graphics, partialTicks) -> {
			CreateMainMenuScreen.PANORAMA.render(screen.getMinecraft().getDeltaFrameTime(), 1);

			//RenderSystem.setShaderTexture(0, CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			graphics.blit(CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES, 0, 0, screen.width, screen.height, 0.0F, 0.0F, 16, 128, 16, 128);

			graphics.fill(0, 0, screen.width, screen.height, 0x90_282c34);
		});

		ConfigScreen.shadowState = AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y);

		BaseConfigScreen.setDefaultActionFor(Create.ID, base -> base
				.withButtonLabels("Client Settings", "World Generation Settings", "Gameplay Settings")
				.withSpecs(AllConfigs.client().specification, AllConfigs.common().specification, AllConfigs.server().specification)
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

		if (mc.options.graphicsMode().get() != GraphicsStatus.FABULOUS)
			return;

		if (AllConfigs.client().ignoreFabulousWarning.get())
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
