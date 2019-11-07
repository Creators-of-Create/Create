package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.modules.ModuleLoadedCondition;
import com.simibubi.create.modules.contraptions.receivers.constructs.MovingConstructHandler;
import com.simibubi.create.modules.logistics.FrequencyHandler;
import com.simibubi.create.modules.schematics.ServerSchematicLoader;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.1.1b";

	public static Logger logger = LogManager.getLogger();
	public static ItemGroup creativeTab = new CreateItemGroup();
	public static ServerSchematicLoader schematicReceiver;
	public static FrequencyHandler frequencyHandler;
	public static MovingConstructHandler constructHandler;

	public static ModConfig config;

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(Create::init);
		modEventBus.addGenericListener(Block.class, Create::registerBlocks);
		modEventBus.addGenericListener(Item.class, Create::registerItems);
		modEventBus.addGenericListener(IRecipeSerializer.class, Create::registerRecipes);
		modEventBus.addGenericListener(TileEntityType.class, Create::registerTileEntities);
		modEventBus.addGenericListener(ContainerType.class, Create::registerContainers);
		modEventBus.addListener(Create::createConfigs);
		CreateClient.addListeners(modEventBus);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CreateConfig.specification);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CreateClientConfig.specification);
	}

	public static void init(final FMLCommonSetupEvent event) {
		schematicReceiver = new ServerSchematicLoader();
		frequencyHandler = new FrequencyHandler();
		constructHandler = new MovingConstructHandler();
		CraftingHelper.register(new ModuleLoadedCondition.Serializer());
		AllPackets.registerPackets();
	}

	public static void registerItems(RegistryEvent.Register<Item> event) {
		AllItems.registerItems(event.getRegistry());
		AllBlocks.registerItemBlocks(event.getRegistry());
	}

	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		AllBlocks.registerBlocks(event.getRegistry());
	}

	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		AllTileEntities.registerTileEntities(event.getRegistry());
	}

	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
		AllContainers.registerContainers(event.getRegistry());
	}

	public static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		AllRecipes.register(event);
	}

	public static void createConfigs(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == CreateClientConfig.specification)
			return;

		config = event.getConfig();
	}

	public static void tick() {
		if (schematicReceiver == null)
			schematicReceiver = new ServerSchematicLoader();
		schematicReceiver.tick();
	}

	public static void shutdown() {
		schematicReceiver.shutdown();
	}

}
