package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.curios.Curios;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours;
import com.simibubi.create.content.logistics.block.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ContraptionMovementSetting;
import com.simibubi.create.foundation.data.AllLangPartials;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.foundation.worldgen.AllFeatures;
import com.simibubi.create.foundation.worldgen.AllOreFeatureConfigEntries;
import com.simibubi.create.foundation.worldgen.AllPlacementModifiers;
import com.simibubi.create.foundation.worldgen.BuiltinRegistration;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Create.ID)
public class Create {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.5.1-unstable";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/** Use the {@link Random} of a local {@link Level} or {@link Entity} or create one */
	@Deprecated
	public static final Random RANDOM = new Random();

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	static {
		REGISTRATE.setTooltipModifierFactory(item -> {
			return new ItemDescription.Modifier(item, Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
		});
	}

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	public Create() {
		onCtor();
	}

	public static void onCtor() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		IEventBus modEventBus = FMLJavaModLoadingContext.get()
			.getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		REGISTRATE.registerEventListeners(modEventBus);

		AllSoundEvents.prepare();
		AllTags.init();
		AllCreativeModeTabs.init();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
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

		ForgeMod.enableMilkFluid();
		CopperRegistries.inject();

		modEventBus.addListener(Create::init);
		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData);
		modEventBus.addListener(AllSoundEvents::register);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));

		Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus, forgeEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

		event.enqueueWork(() -> {
			AttachedRegistry.unwrapAll();
			AllAdvancements.register();
			AllTriggers.register();
			BoilerHeaters.registerDefaults();
			AllFluids.registerFluidInteractions();
		});
	}

	public static void gatherData(GatherDataEvent event) {
		TagGen.datagen();
		DataGenerator gen = event.getGenerator();
		if (event.includeClient()) {
			gen.addProvider(true, new LangMerger(gen, ID, NAME, AllLangPartials.values()));
			gen.addProvider(true, AllSoundEvents.provider(gen));
		}
		if (event.includeServer()) {
			gen.addProvider(true, new AllAdvancements(gen));
			gen.addProvider(true, new StandardRecipeGen(gen));
			gen.addProvider(true, new MechanicalCraftingRecipeGen(gen));
			gen.addProvider(true, new SequencedAssemblyRecipeGen(gen));
			ProcessingRecipeGen.registerAll(gen);
//			AllOreFeatureConfigEntries.gatherData(event);
		}
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
