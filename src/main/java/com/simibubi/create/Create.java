package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.modules.contraptions.receivers.constructs.MovingConstructHandler;
import com.simibubi.create.modules.logistics.FrequencyHandler;
import com.simibubi.create.modules.logistics.InWorldItemProcessingHandler;
import com.simibubi.create.modules.schematics.ServerSchematicLoader;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.0.5";

	public static Logger logger = LogManager.getLogger();
	public static ItemGroup creativeTab = new CreateItemGroup();
	public static ServerSchematicLoader schematicReceiver;
	public static FrequencyHandler frequencyHandler;
	public static InWorldItemProcessingHandler itemProcessingHandler;
	public static MovingConstructHandler constructHandler;

	public static ModConfig config;

	public Create() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CreateConfig.specification);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CreateClientConfig.specification);
	}

	@SubscribeEvent
	public static void init(final FMLCommonSetupEvent event) {
		schematicReceiver = new ServerSchematicLoader();
		itemProcessingHandler = new InWorldItemProcessingHandler();
		frequencyHandler = new FrequencyHandler();
		constructHandler = new MovingConstructHandler();
		AllPackets.registerPackets();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		AllItems.registerItems(event.getRegistry());
		AllBlocks.registerItemBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		AllBlocks.registerBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		AllRecipes.register(event);
	}

	@SubscribeEvent
	public static void createConfigs(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == CreateClientConfig.specification)
			return;

		config = event.getConfig();
	}

	public static void tick() {
		schematicReceiver.tick();
	}

	public static void shutdown() {
		schematicReceiver.shutdown();
	}

}
