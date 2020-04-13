package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.CreateCommand;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.world.AllWorldFeatures;
import com.simibubi.create.modules.ModuleLoadedCondition;
import com.simibubi.create.modules.contraptions.TorquePropagator;
import com.simibubi.create.modules.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.modules.schematics.ServerSchematicLoader;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.LazyValue;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.1.1b";

	public static Logger logger = LogManager.getLogger();
	public static ItemGroup creativeTab = new CreateItemGroup();
	public static ServerSchematicLoader schematicReceiver;
	public static RedstoneLinkNetworkHandler redstoneLinkNetworkHandler;
	public static TorquePropagator torquePropagator;
	public static ServerLagger lagger;
	private static final LazyValue<Registrate> registrate = new LazyValue<>(() -> Registrate.create(ID));

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(Create::init);

		MinecraftForge.EVENT_BUS.addListener(Create::serverStarting);

		AllBlocks.register();
//		modEventBus.addGenericListener(Block.class, AllBlocks::register);
		modEventBus.addGenericListener(Item.class, AllItems::register);
		modEventBus.addGenericListener(IRecipeSerializer.class, AllRecipes::register);
		modEventBus.addGenericListener(TileEntityType.class, AllTileEntities::register);
		modEventBus.addGenericListener(ContainerType.class, AllContainers::register);
		modEventBus.addGenericListener(EntityType.class, AllEntities::register);
		modEventBus.addGenericListener(ParticleType.class, AllParticles::register);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);

		modEventBus.addListener(AllConfigs::onLoad);
		modEventBus.addListener(AllConfigs::onReload);
		CreateClient.addListeners(modEventBus);
	}

	public static void init(final FMLCommonSetupEvent event) {
	    AllConfigs.registerAll();

		schematicReceiver = new ServerSchematicLoader();
		redstoneLinkNetworkHandler = new RedstoneLinkNetworkHandler();
		torquePropagator = new TorquePropagator();
		lagger = new ServerLagger();

		CraftingHelper.register(new ModuleLoadedCondition.Serializer());
		AllPackets.registerPackets();
		AllTriggers.register();
		
		AllWorldFeatures.reload();
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

	public static Registrate registrate() {
	    return registrate.get();
	}
}
