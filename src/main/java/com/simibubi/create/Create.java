package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.foundation.utility.KeyboardHelper;
import com.simibubi.create.modules.schematics.ClientSchematicLoader;
import com.simibubi.create.modules.schematics.ServerSchematicLoader;
import com.simibubi.create.modules.schematics.client.BlueprintHandler;
import com.simibubi.create.modules.schematics.client.SchematicHologram;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.FORGE)
@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.0.3";

	public static Logger logger = LogManager.getLogger();

	public static ItemGroup creativeTab = new CreateItemGroup();
	
	@OnlyIn(Dist.CLIENT)
	public static ClientSchematicLoader cSchematicLoader;
	@OnlyIn(Dist.CLIENT)
	public static KeyBinding TOOL_MENU;
	
	public static ServerSchematicLoader sSchematicLoader;

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::clientInit);
		modEventBus.addListener(this::init);
	}

	private void clientInit(FMLClientSetupEvent event) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			AllItems.initColorHandlers();
			AllTileEntities.registerRenderers();
			cSchematicLoader = new ClientSchematicLoader();
			new SchematicHologram();
			new BlueprintHandler();
			ScrollFixer.init();
			TOOL_MENU = new KeyBinding("Tool Menu (Hold)", KeyboardHelper.LALT, NAME);
			ClientRegistry.registerKeyBinding(TOOL_MENU);
		});
	}

	private void init(final FMLCommonSetupEvent event) {
		AllPackets.registerPackets();
		DistExecutor.runWhenOn(Dist.CLIENT, () -> AllContainers::registerScreenFactories);
		sSchematicLoader = new ServerSchematicLoader();
	}
	
	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		sSchematicLoader.tick();
	}
	
	@SubscribeEvent
	public static void onServerClose(FMLServerStoppingEvent event) {
		sSchematicLoader.shutdown();
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		cSchematicLoader.tick();
	}

	@EventBusSubscriber(bus = Bus.MOD)
	public static class RegistryListener {

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			AllItems.registerItems(event.getRegistry());
			AllBlocks.registerItemBlocks(event.getRegistry());
		}

		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			AllBlocks.registerBlocks(event.getRegistry());
		}
	}
}
