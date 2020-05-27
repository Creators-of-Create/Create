package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperRenderHandler;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.foundation.ResourceReloadHandler;
import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.item.CustomItemModels;
import com.simibubi.create.foundation.item.CustomRenderedItems;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static SuperByteBufferCache bufferCache;
	public static Outliner outliner;

	private static CustomBlockModels customBlockModels;
	private static CustomItemModels customItemModels;
	private static CustomRenderedItems customRenderedItems;
	private static AllColorHandlers colorHandlers;

	public static void addListeners(IEventBus modEventBus) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			modEventBus.addListener(CreateClient::clientInit);
			modEventBus.addListener(CreateClient::onModelBake);
			modEventBus.addListener(CreateClient::onModelRegistry);
			modEventBus.addListener(CreateClient::onTextureStitch);
			modEventBus.addListener(AllParticleTypes::registerFactories);
		});
	}

	public static void clientInit(FMLClientSetupEvent event) {
		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicAndQuillHandler = new SchematicAndQuillHandler();
		outliner = new Outliner();

		bufferCache = new SuperByteBufferCache();
		bufferCache.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		bufferCache.registerCompartment(ContraptionRenderer.CONTRAPTION, 20);

		AllKeys.register();
		AllContainerTypes.registerScreenFactories();
		AllTileEntities.registerRenderers();
		AllEntityTypes.registerRenderers();
		getColorHandler().init();

		IResourceManager resourceManager = Minecraft.getInstance()
			.getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).addReloadListener(new ResourceReloadHandler());
	}

	public static void gameTick() {
		schematicSender.tick();
		schematicAndQuillHandler.tick();
		schematicHandler.tick();
		BeltConnectorHandler.tick();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		WorldshaperRenderHandler.tick();
		BlockzapperRenderHandler.tick();
		KineticDebugger.tick();
		ZapperRenderHandler.tick();
		outliner.tickOutlines();
	}

	@OnlyIn(Dist.CLIENT)
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap()
			.getId()
			.equals(PlayerContainer.BLOCK_ATLAS_TEXTURE))
			return;
		SpriteShifter.getAllTargetSprites()
			.forEach(event::addSprite);
	}

	@OnlyIn(Dist.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		AllBlockPartials.onModelBake(event);

		getCustomBlockModels()
			.foreach((block, modelFunc) -> swapModels(modelRegistry, getAllBlockStateModelLocations(block), modelFunc));
		getCustomItemModels()
			.foreach((item, modelFunc) -> swapModels(modelRegistry, getItemModelLocation(item), modelFunc));
		getCustomRenderedItems().foreach((item, modelFunc) -> {
			swapModels(modelRegistry, getItemModelLocation(item), m -> modelFunc.apply(m)
				.loadPartials(event));
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void onModelRegistry(ModelRegistryEvent event) {
		AllBlockPartials.onModelRegistry(event);

		getCustomRenderedItems().foreach((item, modelFunc) -> modelFunc.apply(null)
			.getModelLocations()
			.forEach(ModelLoader::addSpecialModel));
	}

	@OnlyIn(Dist.CLIENT)
	protected static ModelResourceLocation getItemModelLocation(Item item) {
		return new ModelResourceLocation(item.getRegistryName(), "inventory");
	}

	@OnlyIn(Dist.CLIENT)
	protected static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		block.getStateContainer()
			.getValidStates()
			.forEach(state -> {
				models.add(getBlockModelLocation(block, BlockModelShapes.getPropertyMapString(state.getValues())));
			});
		return models;
	}

	@OnlyIn(Dist.CLIENT)
	protected static ModelResourceLocation getBlockModelLocation(Block block, String suffix) {
		return new ModelResourceLocation(block.getRegistryName(), suffix);
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
		List<ModelResourceLocation> locations, Function<IBakedModel, T> factory) {
		locations.forEach(location -> {
			swapModels(modelRegistry, location, factory);
		});
	}

	@OnlyIn(Dist.CLIENT)
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

	public static AllColorHandlers getColorHandler() {
		if (colorHandlers == null)
			colorHandlers = new AllColorHandlers();
		return colorHandlers;
	}

}
