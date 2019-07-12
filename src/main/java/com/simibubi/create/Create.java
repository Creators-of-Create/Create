package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.networking.Packets;
import com.simibubi.create.schematic.SchematicHologram;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.FORGE)
@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.0.1";

	public static Logger logger = LogManager.getLogger();

	public static ItemGroup creativeTab = new CreateItemGroup();
	
	@OnlyIn(Dist.CLIENT)
	public static ClientSchematicLoader cSchematicLoader;
	
	public static ServerSchematicLoader sSchematicLoader;

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::clientInit);
		modEventBus.addListener(this::init);
	}

	private void clientInit(FMLClientSetupEvent event) {
		AllItems.initColorHandlers();
		AllTileEntities.registerRenderers();
		cSchematicLoader = new ClientSchematicLoader();
		sSchematicLoader = new ServerSchematicLoader();
		new SchematicHologram();
//		ScrollFixer.init();
	}

	private void init(final FMLCommonSetupEvent event) {
		Packets.registerPackets();
		DistExecutor.runWhenOn(Dist.CLIENT, () -> AllContainers::registerScreenFactories);
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
