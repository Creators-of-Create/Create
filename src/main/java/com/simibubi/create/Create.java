package com.simibubi.create;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineModifiers;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
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
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod(Create.ID)
public class Create {

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

	public Create() {
		onCtor();
	}

	public static void onCtor() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

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
		AllConfigs.register(modLoadingContext);
		BlockSpoutingBehaviour.register();

		ForgeMod.enableMilkFluid();

		IEventBus modEventBus = FMLJavaModLoadingContext.get()
			.getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		modEventBus.addListener(Create::init);
		modEventBus.addGenericListener(Feature.class, AllWorldFeatures::registerOreFeatures);
		modEventBus.addGenericListener(FeatureDecorator.class, AllWorldFeatures::registerDecoratorFeatures);
		modEventBus.addGenericListener(RecipeSerializer.class, AllRecipeTypes::register);
		modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);
		modEventBus.addListener(AllConfigs::onLoad);
		modEventBus.addListener(AllConfigs::onReload);
		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData);

		forgeEventBus.addListener(EventPriority.HIGH, Create::onBiomeLoad);
		forgeEventBus.register(CHUNK_UTIL);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

		CHUNK_UTIL.init();

		event.enqueueWork(() -> {
			AllTriggers.register();
			SchematicProcessor.register();
			AllWorldFeatures.registerFeatures();
		});
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		gen.addProvider(new AllAdvancements(gen));
		gen.addProvider(new LangMerger(gen));
		gen.addProvider(AllSoundEvents.provider(gen));
		gen.addProvider(new StandardRecipeGen(gen));
		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
		gen.addProvider(new SequencedAssemblyRecipeGen(gen));
		ProcessingRecipeGen.registerAll(gen);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(CapabilityMinecartController.class);
	}
	
	public static void onBiomeLoad(BiomeLoadingEvent event) {
		AllWorldFeatures.reload(event);
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
