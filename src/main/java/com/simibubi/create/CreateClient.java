package com.simibubi.create;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.render.SBBContraptionManager;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.bell.SoulPulseEffectHandler;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.events.ClientEvents;
import com.simibubi.create.foundation.ClientResourceReloadListener;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.ModelSwapper;
import com.simibubi.create.foundation.utility.ghost.GhostBlocks;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static final SuperByteBufferCache BUFFER_CACHE = new SuperByteBufferCache();
	public static final Outliner OUTLINER = new Outliner();
	public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();
	public static final Screen EMPTY_SCREEN = new Screen(new StringTextComponent("")) {};
	public static final ModelSwapper MODEL_SWAPPER = new ModelSwapper();
	public static final CasingConnectivity CASING_CONNECTIVITY = new CasingConnectivity();

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();

	public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
	public static final PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
	public static final SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(CreateClient::clientInit);
		modEventBus.addListener(ClientEvents::loadCompleted);
		modEventBus.addListener(SpriteShifter::onTextureStitchPre);
		modEventBus.addListener(SpriteShifter::onTextureStitchPost);
		modEventBus.addListener(AllParticleTypes::registerFactories);
		modEventBus.addListener(CreateContexts::flwInit);
		modEventBus.addListener(AllMaterialSpecs::flwInit);
		modEventBus.addListener(ContraptionRenderDispatcher::gatherContext);

		MODEL_SWAPPER.registerListeners(modEventBus);

		ZAPPER_RENDER_HANDLER.registerListeners(forgeEventBus);
		POTATO_CANNON_RENDER_HANDLER.registerListeners(forgeEventBus);

		Minecraft mc = Minecraft.getInstance();

		// null during datagen
		if (mc == null) return;

		IResourceManager resourceManager = mc.getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).registerReloadListener(RESOURCE_RELOAD_LISTENER);
	}

	public static void clientInit(final FMLClientSetupEvent event) {
		BUFFER_CACHE.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		BUFFER_CACHE.registerCompartment(SBBContraptionManager.CONTRAPTION, 20);
		BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		AllKeys.register();
		// AllFluids.assignRenderLayers();
		AllBlockPartials.init();
		AllStitchedTextures.init();

		PonderIndex.register();
		PonderIndex.registerTags();

		UIRenderHelper.init();

		event.enqueueWork(() -> {
			registerLayerRenderers(Minecraft.getInstance()
				.getEntityRenderDispatcher());
		});
	}

	protected static void registerLayerRenderers(EntityRendererManager renderManager) {
		CopperBacktankArmorLayer.registerOnAll(renderManager);
	}

	public static void invalidateRenderers() {
		BUFFER_CACHE.invalidate();

		ContraptionRenderDispatcher.reset();
	}

	public static void checkGraphicsFanciness() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		if (mc.options.graphicsMode != GraphicsFanciness.FABULOUS)
			return;

		if (AllConfigs.CLIENT.ignoreFabulousWarning.get())
			return;

		IFormattableTextComponent text = TextComponentUtils.wrapInSquareBrackets(new StringTextComponent("WARN"))
			.withStyle(TextFormatting.GOLD)
			.append(new StringTextComponent(
				" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.withStyle(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new StringTextComponent("Click here to disable this warning"))));

		mc.gui.handleChat(ChatType.CHAT, text, mc.player.getUUID());
	}

}
