package com.simibubi.create;

import com.simibubi.create.foundation.command.ChunkUtil;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.NonNullLazyValue;

import net.minecraft.data.DataGenerator;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.3";

	public static Logger logger = LogManager.getLogger();
	public static ItemGroup baseCreativeTab = new CreateItemGroup();
	public static ItemGroup palettesCreativeTab = new PalettesItemGroup();

	public static Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static ServerSchematicLoader schematicReceiver;
	public static RedstoneLinkNetworkHandler redstoneLinkNetworkHandler;
	public static TorquePropagator torquePropagator;
	public static ServerLagger lagger;
	public static ChunkUtil chunkUtil;

	private static final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(ID);

	public Create() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
			.getModEventBus();

		AllBlocks.register();
		AllItems.register();
		AllTags.register();
		AllPaletteBlocks.register();
		AllEntityTypes.register();
		AllTileEntities.register();
		AllMovementBehaviours.register();

		modEventBus.addListener(Create::init);
		modEventBus.addGenericListener(IRecipeSerializer.class, AllRecipeTypes::register);
		modEventBus.addGenericListener(ContainerType.class, AllContainerTypes::register);
		modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);
		modEventBus.addListener(AllConfigs::onLoad);
		modEventBus.addListener(AllConfigs::onReload);
		modEventBus.addListener(EventPriority.LOWEST, this::gatherData);

		AllConfigs.register();

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> CreateClient.addClientListeners(modEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		schematicReceiver = new ServerSchematicLoader();
		redstoneLinkNetworkHandler = new RedstoneLinkNetworkHandler();
		torquePropagator = new TorquePropagator();
		lagger = new ServerLagger();

		chunkUtil = new ChunkUtil();
		chunkUtil.init();
		MinecraftForge.EVENT_BUS.register(chunkUtil);

		AllPackets.registerPackets();
		AllTriggers.register();
		AllWorldFeatures.reload();
	}

	public static CreateRegistrate registrate() {
		return registrate.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

	public void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		gen.addProvider(new AllAdvancements(gen));
		gen.addProvider(new LangMerger(gen));
		gen.addProvider(AllSoundEvents.BLAZE_MUNCH.generator(gen));
		gen.addProvider(new StandardRecipeGen(gen));
		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
		ProcessingRecipeGen.registerAll(gen);
	}

}
