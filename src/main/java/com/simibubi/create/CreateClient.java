package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.AtlasStitcher;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.events.ClientEvents;
import com.simibubi.create.foundation.ResourceReloadHandler;
import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.ghost.GhostBlocks;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
	public static final SuperByteBufferCache BUFFER_CACHE = new SuperByteBufferCache();
	public static final Outliner OUTLINER = new Outliner();
	public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();

	private static CustomBlockModels customBlockModels;
	private static CustomItemModels customItemModels;
	private static CustomRenderedItems customRenderedItems;
	private static CasingConnectivity casingConnectivity;

	public static void addClientListeners(IEventBus modEventBus) {
		modEventBus.addListener(CreateClient::clientInit);
		modEventBus.addListener(CreateClient::onTextureStitch);
		modEventBus.addListener(CreateClient::onModelRegistry);
		modEventBus.addListener(CreateClient::onModelBake);
		modEventBus.addListener(AllParticleTypes::registerFactories);
		modEventBus.addListener(ClientEvents::loadCompleted);
		modEventBus.addListener(CreateContexts::flwInit);
		modEventBus.addListener(AllMaterialSpecs::flwInit);
	}

	public static void clientInit(FMLClientSetupEvent event) {
		BUFFER_CACHE.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		BUFFER_CACHE.registerCompartment(ContraptionRenderDispatcher.CONTRAPTION, 20);
		BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		AllKeys.register();
		// AllFluids.assignRenderLayers();
		AllBlockPartials.clientInit();
		AllStitchedTextures.init();

		PonderIndex.register();
		PonderIndex.registerTags();

		UIRenderHelper.init();

		IResourceManager resourceManager = Minecraft.getInstance()
				.getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).addReloadListener(new ResourceReloadHandler());

		event.enqueueWork(() -> {
			CopperBacktankArmorLayer.register();
		});
	}

	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap()
				.getId()
				.equals(PlayerContainer.BLOCK_ATLAS_TEXTURE))
			return;
		SpriteShifter.getAllTargetSprites()
				.forEach(event::addSprite);
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		PartialModel.onModelRegistry(event);

		getCustomRenderedItems().foreach((item, modelFunc) -> modelFunc.apply(null)
				.getModelLocations()
				.forEach(ModelLoader::addSpecialModel));
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		PartialModel.onModelBake(event);

		getCustomBlockModels()
				.foreach((block, modelFunc) -> swapModels(modelRegistry, getAllBlockStateModelLocations(block), modelFunc));
		getCustomItemModels()
				.foreach((item, modelFunc) -> swapModels(modelRegistry, getItemModelLocation(item), modelFunc));
		getCustomRenderedItems().foreach((item, modelFunc) -> {
			swapModels(modelRegistry, getItemModelLocation(item), m -> modelFunc.apply(m)
				.loadPartials(event));
		});
	}

	protected static ModelResourceLocation getItemModelLocation(Item item) {
		return new ModelResourceLocation(item.getRegistryName(), "inventory");
	}

	protected static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		block.getStateContainer()
			.getValidStates()
			.forEach(state -> {
				models.add(getBlockModelLocation(block, BlockModelShapes.getPropertyMapString(state.getValues())));
			});
		return models;
	}

	protected static ModelResourceLocation getBlockModelLocation(Block block, String suffix) {
		return new ModelResourceLocation(block.getRegistryName(), suffix);
	}

	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
		List<ModelResourceLocation> locations, Function<IBakedModel, T> factory) {
		locations.forEach(location -> {
			swapModels(modelRegistry, location, factory);
		});
	}

	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
		ModelResourceLocation location, Function<IBakedModel, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

	public static CustomItemModels getCustomItemModels() {
		if (customItemModels == null)
			customItemModels = new CustomItemModels();
		return customItemModels;
	}

	public static CustomRenderedItems getCustomRenderedItems() {
		if (customRenderedItems == null)
			customRenderedItems = new CustomRenderedItems();
		return customRenderedItems;
	}

	public static CustomBlockModels getCustomBlockModels() {
		if (customBlockModels == null)
			customBlockModels = new CustomBlockModels();
		return customBlockModels;
	}

	public static CasingConnectivity getCasingConnectivity() {
		if (casingConnectivity == null)
			casingConnectivity = new CasingConnectivity();
		return casingConnectivity;
	}

	public static void invalidateRenderers() {
		BUFFER_CACHE.invalidate();

		ContraptionRenderDispatcher.invalidateAll();
	}

	public static void checkGraphicsFanciness() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		if (mc.gameSettings.graphicsMode != GraphicsFanciness.FABULOUS)
			return;

		if (AllConfigs.CLIENT.ignoreFabulousWarning.get())
			return;

		IFormattableTextComponent text = TextComponentUtils.bracketed(new StringTextComponent("WARN"))
			.formatted(TextFormatting.GOLD)
			.append(new StringTextComponent(
				" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new StringTextComponent("Click here to disable this warning"))));

		mc.ingameGUI.addChatMessage(ChatType.CHAT, text, mc.player.getUniqueID());
	}
}
