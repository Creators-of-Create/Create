package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.curios.Curios;
import com.simibubi.create.content.contraptions.ContraptionMovementSetting;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.equipment.potatoCannon.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.TorquePropagator;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.data.AllLangPartials;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.RecipeSerializerTagGen;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.infrastructure.command.ServerLagger;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.AllOreFeatureConfigEntries;
import com.simibubi.create.infrastructure.worldgen.AllPlacementModifiers;
import com.simibubi.create.infrastructure.worldgen.BuiltinRegistration;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
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
	public static final String VERSION = "0.5.1e";

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
		AllPackets.registerPackets();
		AllOreFeatureConfigEntries.init();
		AllFeatures.register(modEventBus);
		AllPlacementModifiers.register(modEventBus);
		BuiltinRegistration.register(modEventBus);

		AllConfigs.register(modLoadingContext);

		// FIXME: some of these registrations are not thread-safe
		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		ContraptionMovementSetting.registerDefaults();
		AllArmInteractionPointTypes.register();
		AllFanProcessingTypes.register();
		BlockSpoutingBehaviour.registerDefaults();
		BogeySizes.init();
		AllBogeyStyles.register();
		// ----

		ComputerCraftProxy.register();

		ForgeMod.enableMilkFluid();
		CopperRegistries.inject();

		modEventBus.addListener(Create::init);
		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData);
		modEventBus.addGenericListener(SoundEvent.class, AllSoundEvents::register);

		forgeEventBus.addListener(EventPriority.HIGH, SlidingDoorBlock::stopItQuark);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus));

		// FIXME: this is not thread-safe
		Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus, forgeEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			// TODO: custom registration should all happen in one place
			// Most registration happens in the constructor.
			// These registrations use Create's registered objects directly so they must run after registration has finished.
			BuiltinPotatoProjectileTypes.register();
			BoilerHeaters.registerDefaults();
			// --

			AttachedRegistry.unwrapAll();
			AllAdvancements.register();
			AllTriggers.register();
		});
	}

	public static void gatherData(GatherDataEvent event) {
		TagGen.datagen();
		DataGenerator gen = event.getGenerator();
		if (event.includeClient()) {
			gen.addProvider(new LangMerger(gen, ID, NAME, AllLangPartials.values()));
			gen.addProvider(AllSoundEvents.provider(gen));
		}
		if (event.includeServer()) {
			gen.addProvider(new RecipeSerializerTagGen(gen, event.getExistingFileHelper()));
			gen.addProvider(new AllAdvancements(gen));
			gen.addProvider(new StandardRecipeGen(gen));
			gen.addProvider(new MechanicalCraftingRecipeGen(gen));
			gen.addProvider(new SequencedAssemblyRecipeGen(gen));
			ProcessingRecipeGen.registerAll(gen);
//			AllOreFeatureConfigEntries.gatherData(event);
		}
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
