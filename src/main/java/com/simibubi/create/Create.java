package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.CreateCommand;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.NonNullLazyValue;

import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.2.3";

	public static Logger logger = LogManager.getLogger();
	public static ItemGroup baseCreativeTab = new CreateItemGroup();
	public static ItemGroup palettesCreativeTab = new PalettesItemGroup();
	
	public static ServerSchematicLoader schematicReceiver;
	public static RedstoneLinkNetworkHandler redstoneLinkNetworkHandler;
	public static TorquePropagator torquePropagator;
	public static ServerLagger lagger;
	
	private static final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(ID);

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(Create::init);

		MinecraftForge.EVENT_BUS.addListener(Create::serverStarting);

		AllBlocks.register();
		AllPaletteBlocks.register();
		
		modEventBus.addGenericListener(Item.class, AllItems::register);
		modEventBus.addGenericListener(IRecipeSerializer.class, AllRecipeTypes::register);
		modEventBus.addGenericListener(TileEntityType.class, AllTileEntities::register);
		modEventBus.addGenericListener(ContainerType.class, AllContainerTypes::register);
		modEventBus.addGenericListener(EntityType.class, AllEntityTypes::register);
		modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);
		modEventBus.addListener(AllConfigs::onLoad);
		modEventBus.addListener(AllConfigs::onReload);
		
		CreateClient.addListeners(modEventBus);
		AllConfigs.registerClientCommon();
	}

	public static void init(final FMLCommonSetupEvent event) {
		schematicReceiver = new ServerSchematicLoader();
		redstoneLinkNetworkHandler = new RedstoneLinkNetworkHandler();
		torquePropagator = new TorquePropagator();
		lagger = new ServerLagger();

		AllPackets.registerPackets();
		AllTriggers.register();
		
		AllWorldFeatures.reload();
		AllConfigs.registerServer();
	}

	public static void serverStarting(FMLServerStartingEvent event) {
		new CreateCommand(event.getCommandDispatcher());
	}

	public static void tick() {
		if (schematicReceiver == null)
			schematicReceiver = new ServerSchematicLoader();
		schematicReceiver.tick();

		lagger.tick();
	}

	public static void shutdown() {
		schematicReceiver.shutdown();
	}

	public static CreateRegistrate registrate() {
	    return registrate.get();
	}
	
	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}
	
}
