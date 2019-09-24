package com.simibubi.create;

import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.modules.contraptions.CachedBufferReloader;
import com.simibubi.create.modules.contraptions.receivers.EncasedFanParticleHandler;
import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockModel;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunModel;
import com.simibubi.create.modules.curiosities.symmetry.client.SymmetryWandModel;
import com.simibubi.create.modules.schematics.ClientSchematicLoader;
import com.simibubi.create.modules.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.modules.schematics.client.SchematicHandler;
import com.simibubi.create.modules.schematics.client.SchematicHologram;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD)
public class CreateClient {

	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicHologram schematicHologram;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static EncasedFanParticleHandler fanParticles;

	public static ModConfig config;

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicHologram = new SchematicHologram();
		schematicAndQuillHandler = new SchematicAndQuillHandler();
		fanParticles = new EncasedFanParticleHandler();

		AllKeys.register();
		AllContainers.registerScreenFactories();
		AllTileEntities.registerRenderers();
		AllItems.registerColorHandlers();
		AllBlocks.registerColorHandlers();

		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager)
			((IReloadableResourceManager) resourceManager).addReloadListener(new CachedBufferReloader());
	}

	@SubscribeEvent
	public static void createConfigs(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == CreateConfig.specification)
			return;

		config = event.getConfig();
	}

	public static void gameTick() {
		schematicSender.tick();
		schematicAndQuillHandler.tick();
		schematicHandler.tick();
		schematicHologram.tick();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();

		swapModels(modelRegistry, getItemModelLocation(AllItems.SYMMETRY_WAND),
				t -> new SymmetryWandModel(t).loadPartials(event));
		swapModels(modelRegistry, getItemModelLocation(AllItems.PLACEMENT_HANDGUN),
				t -> new BuilderGunModel(t).loadPartials(event));
		swapModels(modelRegistry,
				getBlockModelLocation(AllBlocks.WINDOW_IN_A_BLOCK,
						BlockModelShapes
								.getPropertyMapString(AllBlocks.WINDOW_IN_A_BLOCK.get().getDefaultState().getValues())),
				WindowInABlockModel::new);
		swapModels(modelRegistry,
				getBlockModelLocation(AllBlocks.WINDOW_IN_A_BLOCK,
						BlockModelShapes.getPropertyMapString(AllBlocks.WINDOW_IN_A_BLOCK.get().getDefaultState()
								.with(BlockStateProperties.WATERLOGGED, true).getValues())),
				WindowInABlockModel::new);

	}

	protected static ModelResourceLocation getItemModelLocation(AllItems item) {
		return new ModelResourceLocation(item.item.getRegistryName(), "inventory");
	}

	protected static ModelResourceLocation getBlockModelLocation(AllBlocks block, String suffix) {
		return new ModelResourceLocation(block.block.getRegistryName(), suffix);
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
			ModelResourceLocation location, Function<IBakedModel, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

}
