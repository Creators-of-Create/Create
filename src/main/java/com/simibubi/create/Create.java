package com.simibubi.create;

import java.util.Random;

import io.github.tropheusj.milk.Milk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineInteractions;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.events.CommonEvents;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.NonNullLazyValue;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.4d";

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
	public static final Random RANDOM = new Random();

	private static final NonNullLazyValue<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(ID);

	@Override
	public void onInitialize() {
		onCtor();
	}

	public static void onCtor() {
		AllConfigs.register();
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
		FurnaceEngineInteractions.registerDefaults();
		BlockSpoutingBehaviour.register();

		Milk.enableMilkFluid();

		CopperRegistries.inject();

//		GatherDataEvent.EVENT.register(Create::gatherData);

		AllRecipeTypes.register();
		AllParticleTypes.register();
		AllSoundEvents.register();

//		forgeEventBus.register(CHUNK_UTIL);
//		CHUNK_UTIL.fabricInitEvents();

		// handled as ClientModInitializer
//		EnvExecutor.runWhenOn(EnvType.CLIENT,
//			() -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));


		REGISTRATE.get().register();
		init();
		CommonEvents.register();
		AllWorldFeatures.registerOreFeatures();

		AllTileEntities.registerStorages();
		AllPackets.channel.initServerListener();
	}

	public static void init() {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

//		event.enqueueWork(() -> {
			AllTriggers.register();
			SchematicProcessor.register();
			AllWorldFeatures.registerFeatures();
			AllWorldFeatures.registerPlacementTypes();
//		});
	}

	public static void gatherData(FabricDataGenerator gen, ExistingFileHelper helper) { // datagen
		gen.addProvider(new AllAdvancements(gen));
		gen.addProvider(new LangMerger(gen));
		gen.addProvider(AllSoundEvents.provider(gen));
		gen.addProvider(new StandardRecipeGen(gen));
		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
		gen.addProvider(new SequencedAssemblyRecipeGen(gen));
		ProcessingRecipeGen.registerAll(gen);
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
