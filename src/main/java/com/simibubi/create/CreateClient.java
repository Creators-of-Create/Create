package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.foundation.block.IHaveCustomBlockModel;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.ChassisRangeDisplay;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionRenderer;
import com.simibubi.create.modules.schematics.ClientSchematicLoader;
import com.simibubi.create.modules.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.modules.schematics.client.SchematicHandler;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
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

	public static void addListeners(IEventBus modEventBus) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			modEventBus.addListener(CreateClient::clientInit);
			modEventBus.addListener(CreateClient::onModelBake);
			modEventBus.addListener(CreateClient::onModelRegistry);
			modEventBus.addListener(CreateClient::onTextureStitch);
			modEventBus.addListener(AllParticles::registerFactories);
		});
	}

	public static void clientInit(FMLClientSetupEvent event) {
		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicAndQuillHandler = new SchematicAndQuillHandler();

		bufferCache = new SuperByteBufferCache();
		bufferCache.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		bufferCache.registerCompartment(ContraptionRenderer.CONTRAPTION, 20);

		AllKeys.register();
		AllContainers.registerScreenFactories();
		AllTileEntities.registerRenderers();
		AllItems.registerColorHandlers();
		AllBlocks.registerColorHandlers();
		AllEntities.registerRenderers();

		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).addReloadListener(new ResourceReloadHandler());
	}

	public static void gameTick() {
		schematicSender.tick();
		schematicAndQuillHandler.tick();
		schematicHandler.tick();
		ChassisRangeDisplay.clientTick();
	}

	@OnlyIn(Dist.CLIENT)
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap().getId().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE))
			return;

		event.addSprite(new ResourceLocation(Create.ID, "block/belt_animated"));
		for (AllBlocks allBlocks : AllBlocks.values()) {
			Block block = allBlocks.get();
			if (block instanceof IHaveConnectedTextures)
				for (SpriteShiftEntry spriteShiftEntry : ((IHaveConnectedTextures) block).getBehaviour()
						.getAllCTShifts())
					event.addSprite(spriteShiftEntry.getTargetResourceLocation());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		AllBlockPartials.onModelBake(event);

		for (AllBlocks allBlocks : AllBlocks.values()) {
			Block block = allBlocks.get();
			if (block instanceof IHaveCustomBlockModel)
				swapModels(modelRegistry, getAllBlockStateModelLocations(allBlocks),
						((IHaveCustomBlockModel) block)::createModel);
		}

		for (AllItems item : AllItems.values()) {
			if (item.get() instanceof IHaveCustomItemModel)
				swapModels(modelRegistry, getItemModelLocation(item),
						m -> ((IHaveCustomItemModel) item.get()).createModel(m).loadPartials(event));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void onModelRegistry(ModelRegistryEvent event) {
		AllBlockPartials.onModelRegistry(event);

		// Register submodels for custom rendered item models
		for (AllItems item : AllItems.values()) {
			if (item.get() instanceof IHaveCustomItemModel)
				((IHaveCustomItemModel) item.get()).createModel(null).getModelLocations()
						.forEach(ModelLoader::addSpecialModel);
		}
	}

	@OnlyIn(Dist.CLIENT)
	protected static ModelResourceLocation getItemModelLocation(AllItems item) {
		return new ModelResourceLocation(item.get().getRegistryName(), "inventory");
	}

	@OnlyIn(Dist.CLIENT)
	protected static List<ModelResourceLocation> getAllBlockStateModelLocations(AllBlocks block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		block.get().getStateContainer().getValidStates().forEach(state -> {
			models.add(getBlockModelLocation(block, BlockModelShapes.getPropertyMapString(state.getValues())));
		});
		return models;
	}

	@OnlyIn(Dist.CLIENT)
	protected static ModelResourceLocation getBlockModelLocation(AllBlocks block, String suffix) {
		return new ModelResourceLocation(block.get().getRegistryName(), suffix);
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
			ModelResourceLocation location, Function<IBakedModel, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
			List<ModelResourceLocation> locations, Function<IBakedModel, T> factory) {
		locations.forEach(location -> {
			swapModels(modelRegistry, location, factory);
		});
	}

}
