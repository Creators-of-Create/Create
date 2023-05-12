package com.simibubi.create;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.curios.Curios;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.curiosities.deco.SlidingDoorBlock;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours;
import com.simibubi.create.content.logistics.block.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ContraptionMovementSetting;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.CreateRegistry;
import com.simibubi.create.foundation.worldgen.AllFeatures;
import com.simibubi.create.foundation.worldgen.AllOreFeatureConfigEntries;
import com.simibubi.create.foundation.worldgen.AllPlacementModifiers;
import com.simibubi.create.foundation.worldgen.BuiltinRegistration;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
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
	public static final String VERSION = "0.5e";

	public static final Logger LOGGER = LogManager.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static final CreativeModeTab BASE_CREATIVE_TAB = new CreateItemGroup();
	public static final CreativeModeTab PALETTES_CREATIVE_TAB = new PalettesItemGroup();

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ServerLagger LAGGER = new ServerLagger();
	/** Use the {@link Random} of a local {@link Level} or {@link Entity} or create one */
	@Deprecated
	public static final Random RANDOM = new Random();

	private static final NonNullSupplier<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(ID);

	public Create() {
		onCtor();
	}

	public static void onCtor() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		IEventBus modEventBus = FMLJavaModLoadingContext.get()
			.getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		AllSoundEvents.prepare();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllTags.register();
		AllPaletteBlocks.register();
		AllContainerTypes.register();
		AllEntityTypes.register();
		AllTileEntities.register();
		AllEnchantments.register();
		AllRecipeTypes.register(modEventBus);
		AllParticleTypes.register(modEventBus);
		AllStructureProcessorTypes.register(modEventBus);
		AllEntityDataSerializers.register(modEventBus);
		AllOreFeatureConfigEntries.init();
		AllFeatures.register(modEventBus);
		AllPlacementModifiers.register(modEventBus);
		BuiltinRegistration.register(modEventBus);

		AllConfigs.register(modLoadingContext);

		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		ContraptionMovementSetting.registerDefaults();
		AllArmInteractionPointTypes.register();
		BlockSpoutingBehaviour.registerDefaults();
		ComputerCraftProxy.register();

		ForgeMod.enableMilkFluid();
		CopperRegistries.inject();

		modEventBus.addListener(Create::init);
		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);

		forgeEventBus.addListener(EventPriority.HIGH, SlidingDoorBlock::stopItQuark);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));

		Mods.CURIOS.executeIfInstalled(() -> Curios::init);
	}

	public static void init(final FMLCommonSetupEvent event) {
		CreateRegistry.unwrapAll();
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

		event.enqueueWork(() -> {
			AllAdvancements.register();
			AllTriggers.register();
			BoilerHeaters.registerDefaults();
		});
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		if (event.includeClient()) {
			gen.addProvider(new LangMerger(gen));
			gen.addProvider(AllSoundEvents.provider(gen));
		}
		if (event.includeServer()) {
			gen.addProvider(new AllAdvancements(gen));
			gen.addProvider(new StandardRecipeGen(gen));
			gen.addProvider(new MechanicalCraftingRecipeGen(gen));
			gen.addProvider(new SequencedAssemblyRecipeGen(gen));
			ProcessingRecipeGen.registerAll(gen);
//			AllOreFeatureConfigEntries.gatherData(event);
		}
	}

	public static CreateRegistrate registrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
