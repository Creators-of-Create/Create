package com.simibubi.create;

import java.util.Random;

import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.events.CommonEvents;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineModifiers;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.ChunkUtil;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.NonNullLazyValue;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.4-unstable";

	public static final Logger LOGGER = LogManager.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static final CreativeModeTab BASE_CREATIVE_TAB = new CreateItemGroup();
	public static final CreativeModeTab PALETTES_CREATIVE_TAB = new PalettesItemGroup();

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final ServerLagger LAGGER = new ServerLagger();
	public static final ChunkUtil CHUNK_UTIL = new ChunkUtil();
	public static final Random RANDOM = new Random();

	private static final NonNullLazyValue<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(ID);

	public void onInitialize() {
		onCtor();
	}

	public static void onCtor() {
		AllConfigs.register();
		AllConfigs.onLoad();
		AllSoundEvents.prepare();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllTags.register();
		AllPaletteBlocks.register();
		AllContainerTypes.register();
		AllEntityTypes.register();
		AllTileEntities.register();
		AllMovementBehaviours.register();
		AllInteractionBehaviours.register();
		AllWorldFeatures.register();
		AllEnchantments.register();
		FurnaceEngineModifiers.register();
		BlockSpoutingBehaviour.register();

//		ForgeMod.enableMilkFluid(); // FIXME PORT: milk

//		IEventBus modEventBus = FMLJavaModLoadingContext.get()
//			.getModEventBus();
//		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

//		modEventBus.addListener(Create::init);
		init();
//		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData);
		// datagen, not needed
//		modEventBus.addGenericListener(Feature.class, AllWorldFeatures::registerOreFeatures);
		AllWorldFeatures.registerOreFeatures();
//		modEventBus.addGenericListener(FeatureDecorator.class, AllWorldFeatures::registerDecoratorFeatures);
		AllWorldFeatures.registerDecoratorFeatures();
//		modEventBus.addGenericListener(RecipeSerializer.class, AllRecipeTypes::register);
		AllRecipeTypes.register();
//		modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
		AllParticleTypes.register();
//		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);
		AllSoundEvents.register();

//		forgeEventBus.register(CHUNK_UTIL);
		CHUNK_UTIL.fabricInitEvents();

		// handled as ClientModInitializer
//		EnvExecutor.runWhenOn(EnvType.CLIENT,
//			() -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));

		CommonEvents.register();

		REGISTRATE.get().register();
	}

	public static void init() {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

		CHUNK_UTIL.init();

//		event.enqueueWork(() -> {
			AllTriggers.register();
			SchematicProcessor.register();
			AllWorldFeatures.registerFeatures();
//		});
	}

//	public static void gatherData(GatherDataEvent event) { // datagen
//		DataGenerator gen = event.getGenerator();
//		gen.addProvider(new AllAdvancements(gen));
//		gen.addProvider(new LangMerger(gen));
//		gen.addProvider(AllSoundEvents.provider(gen));
//		gen.addProvider(new StandardRecipeGen(gen));
//		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
//		gen.addProvider(new SequencedAssemblyRecipeGen(gen));
//		ProcessingRecipeGen.registerAll(gen);
//	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
