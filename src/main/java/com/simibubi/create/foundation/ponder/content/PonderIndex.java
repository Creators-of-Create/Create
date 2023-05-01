package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackMaterial;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.foundation.ponder.content.fluid.DrainScenes;
import com.simibubi.create.foundation.ponder.content.fluid.FluidMovementActorScenes;
import com.simibubi.create.foundation.ponder.content.fluid.FluidTankScenes;
import com.simibubi.create.foundation.ponder.content.fluid.HosePulleyScenes;
import com.simibubi.create.foundation.ponder.content.fluid.PipeScenes;
import com.simibubi.create.foundation.ponder.content.fluid.PumpScenes;
import com.simibubi.create.foundation.ponder.content.fluid.SpoutScenes;
import com.simibubi.create.foundation.ponder.content.trains.TrackObserverScenes;
import com.simibubi.create.foundation.ponder.content.trains.TrackScenes;
import com.simibubi.create.foundation.ponder.content.trains.TrainScenes;
import com.simibubi.create.foundation.ponder.content.trains.TrainSignalScenes;
import com.simibubi.create.foundation.ponder.content.trains.TrainStationScenes;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Collectors;

public class PonderIndex {

	static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(Create.ID);

	public static final boolean REGISTER_DEBUG_SCENES = false;

	public static void register() {
		// Register storyboards here
		// (!) Added entries require re-launch
		// (!) Modifications inside storyboard methods only require re-opening the ui

		HELPER.forComponents(AllBlocks.SHAFT)
			.addStoryBoard("shaft/relay", KineticsScenes::shaftAsRelay, PonderTag.KINETIC_RELAYS);
		HELPER.forComponents(AllBlocks.SHAFT, AllBlocks.ANDESITE_ENCASED_SHAFT, AllBlocks.BRASS_ENCASED_SHAFT)
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased);

		HELPER.forComponents(AllBlocks.COGWHEEL)
			.addStoryBoard("cog/small", KineticsScenes::cogAsRelay, PonderTag.KINETIC_RELAYS)
			.addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp)
			.addStoryBoard("cog/encasing", KineticsScenes::cogwheelsCanBeEncased);

		HELPER.forComponents(AllBlocks.LARGE_COGWHEEL)
			.addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp)
			.addStoryBoard("cog/large", KineticsScenes::largeCogAsRelay, PonderTag.KINETIC_RELAYS)
			.addStoryBoard("cog/encasing", KineticsScenes::cogwheelsCanBeEncased);

		HELPER.forComponents(AllItems.BELT_CONNECTOR)
			.addStoryBoard("belt/connect", BeltScenes::beltConnector, PonderTag.KINETIC_RELAYS)
			.addStoryBoard("belt/directions", BeltScenes::directions)
			.addStoryBoard("belt/transport", BeltScenes::transport, PonderTag.LOGISTICS)
			.addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

		HELPER.forComponents(AllBlocks.ANDESITE_CASING, AllBlocks.BRASS_CASING)
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased)
			.addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

		HELPER.forComponents(AllBlocks.GEARBOX, AllItems.VERTICAL_GEARBOX)
			.addStoryBoard("gearbox", KineticsScenes::gearbox, PonderTag.KINETIC_RELAYS);

		HELPER.addStoryBoard(AllBlocks.CLUTCH, "clutch", KineticsScenes::clutch, PonderTag.KINETIC_RELAYS);
		HELPER.addStoryBoard(AllBlocks.GEARSHIFT, "gearshift", KineticsScenes::gearshift, PonderTag.KINETIC_RELAYS);

		HELPER.forComponents(AllBlocks.SEQUENCED_GEARSHIFT)
			.addStoryBoard("sequenced_gearshift", KineticsScenes::sequencedGearshift);

		HELPER.forComponents(AllBlocks.ENCASED_FAN)
			.addStoryBoard("fan/direction", FanScenes::direction, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("fan/processing", FanScenes::processing);

		HELPER.forComponents(AllBlocks.CREATIVE_MOTOR)
			.addStoryBoard("creative_motor", KineticsScenes::creativeMotor, PonderTag.KINETIC_SOURCES)
			.addStoryBoard("creative_motor_mojang", KineticsScenes::creativeMotorMojang);
		HELPER.addStoryBoard(AllBlocks.WATER_WHEEL, "water_wheel", KineticsScenes::waterWheel,
			PonderTag.KINETIC_SOURCES);
		HELPER.addStoryBoard(AllBlocks.HAND_CRANK, "hand_crank", KineticsScenes::handCrank, PonderTag.KINETIC_SOURCES);

		HELPER.addStoryBoard(AllBlocks.COPPER_VALVE_HANDLE, "valve_handle", KineticsScenes::valveHandle,
			PonderTag.KINETIC_SOURCES);
		HELPER.forComponents(AllBlocks.DYED_VALVE_HANDLES.toArray())
			.addStoryBoard("valve_handle", KineticsScenes::valveHandle);

		HELPER.addStoryBoard(AllBlocks.ENCASED_CHAIN_DRIVE, "chain_drive/relay", ChainDriveScenes::chainDriveAsRelay,
			PonderTag.KINETIC_RELAYS);
		HELPER.forComponents(AllBlocks.ENCASED_CHAIN_DRIVE, AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
			.addStoryBoard("chain_drive/gearshift", ChainDriveScenes::adjustableChainGearshift);

		HELPER.forComponents(AllBlocks.ROTATION_SPEED_CONTROLLER)
			.addStoryBoard("speed_controller", KineticsScenes::speedController);

		// Gauges
		HELPER.addStoryBoard(AllBlocks.SPEEDOMETER, "gauges", KineticsScenes::speedometer);
		HELPER.addStoryBoard(AllBlocks.STRESSOMETER, "gauges", KineticsScenes::stressometer);

		// Item Processing
		HELPER.addStoryBoard(AllBlocks.MILLSTONE, "millstone", ProcessingScenes::millstone);
		HELPER.addStoryBoard(AllBlocks.CRUSHING_WHEEL, "crushing_wheel", ProcessingScenes::crushingWheels);
		HELPER.addStoryBoard(AllBlocks.MECHANICAL_MIXER, "mechanical_mixer/mixing", ProcessingScenes::mixing);
		HELPER.forComponents(AllBlocks.MECHANICAL_PRESS)
			.addStoryBoard("mechanical_press/pressing", ProcessingScenes::pressing)
			.addStoryBoard("mechanical_press/compacting", ProcessingScenes::compacting);
		HELPER.forComponents(AllBlocks.BASIN)
			.addStoryBoard("basin", ProcessingScenes::basin)
			.addStoryBoard("mechanical_mixer/mixing", ProcessingScenes::mixing)
			.addStoryBoard("mechanical_press/compacting", ProcessingScenes::compacting);
		HELPER.addStoryBoard(AllItems.EMPTY_BLAZE_BURNER, "empty_blaze_burner", ProcessingScenes::emptyBlazeBurner);
		HELPER.addStoryBoard(AllBlocks.BLAZE_BURNER, "blaze_burner", ProcessingScenes::blazeBurner);
		HELPER.addStoryBoard(AllBlocks.DEPOT, "depot", BeltScenes::depot);
		HELPER.forComponents(AllBlocks.WEIGHTED_EJECTOR)
			.addStoryBoard("weighted_ejector/eject", EjectorScenes::ejector)
			.addStoryBoard("weighted_ejector/split", EjectorScenes::splitY)
			.addStoryBoard("weighted_ejector/redstone", EjectorScenes::redstone);

		// Crafters
		HELPER.forComponents(AllBlocks.MECHANICAL_CRAFTER)
			.addStoryBoard("mechanical_crafter/setup", CrafterScenes::setup)
			.addStoryBoard("mechanical_crafter/connect", CrafterScenes::connect);
		HELPER.forComponents(AllBlocks.MECHANICAL_CRAFTER, AllItems.CRAFTER_SLOT_COVER)
			.addStoryBoard("mechanical_crafter/covers", CrafterScenes::covers);

		// Vaults
		HELPER.forComponents(AllBlocks.ITEM_VAULT)
			.addStoryBoard("item_vault/storage", ItemVaultScenes::storage, PonderTag.LOGISTICS)
			.addStoryBoard("item_vault/sizes", ItemVaultScenes::sizes);

		// Chutes
		HELPER.forComponents(AllBlocks.CHUTE)
			.addStoryBoard("chute/downward", ChuteScenes::downward, PonderTag.LOGISTICS)
			.addStoryBoard("chute/upward", ChuteScenes::upward);
		HELPER.forComponents(AllBlocks.CHUTE, AllBlocks.SMART_CHUTE)
			.addStoryBoard("chute/smart", ChuteScenes::smart);

		// Funnels
		HELPER.addStoryBoard(AllBlocks.BRASS_FUNNEL, "funnels/brass", FunnelScenes::brass);
		HELPER.forComponents(AllBlocks.ANDESITE_FUNNEL, AllBlocks.BRASS_FUNNEL)
			.addStoryBoard("funnels/intro", FunnelScenes::intro, PonderTag.LOGISTICS)
			.addStoryBoard("funnels/direction", FunnelScenes::directionality)
			.addStoryBoard("funnels/compat", FunnelScenes::compat)
			.addStoryBoard("funnels/redstone", FunnelScenes::redstone)
			.addStoryBoard("funnels/transposer", FunnelScenes::transposer);
		HELPER.addStoryBoard(AllBlocks.ANDESITE_FUNNEL, "funnels/brass", FunnelScenes::brass);

		// Tunnels
		HELPER.addStoryBoard(AllBlocks.ANDESITE_TUNNEL, "tunnels/andesite", TunnelScenes::andesite);
		HELPER.forComponents(AllBlocks.BRASS_TUNNEL)
			.addStoryBoard("tunnels/brass", TunnelScenes::brass)
			.addStoryBoard("tunnels/brass_modes", TunnelScenes::brassModes);

		// Chassis & Super Glue
		HELPER.forComponents(AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
			.addStoryBoard("chassis/linear_group", ChassisScenes::linearGroup, PonderTag.CONTRAPTION_ASSEMBLY)
			.addStoryBoard("chassis/linear_attachment", ChassisScenes::linearAttachement);
		HELPER.forComponents(AllBlocks.RADIAL_CHASSIS)
			.addStoryBoard("chassis/radial", ChassisScenes::radial, PonderTag.CONTRAPTION_ASSEMBLY);
		HELPER.forComponents(AllItems.SUPER_GLUE)
			.addStoryBoard("super_glue", ChassisScenes::superGlue, PonderTag.CONTRAPTION_ASSEMBLY);
		HELPER.forComponents(AllBlocks.STICKER)
			.addStoryBoard("sticker", RedstoneScenes::sticker, PonderTag.CONTRAPTION_ASSEMBLY);

		// Mechanical Arm
		HELPER.forComponents(AllBlocks.MECHANICAL_ARM)
			.addStoryBoard("mechanical_arm/setup", ArmScenes::setup, PonderTag.ARM_TARGETS)
			.addStoryBoard("mechanical_arm/filter", ArmScenes::filtering)
			.addStoryBoard("mechanical_arm/modes", ArmScenes::modes)
			.addStoryBoard("mechanical_arm/redstone", ArmScenes::redstone);

		// Mechanical Piston
		HELPER.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.addStoryBoard("mechanical_piston/anchor", PistonScenes::movement, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR);
		HELPER
			.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON,
				AllBlocks.PISTON_EXTENSION_POLE)
			.addStoryBoard("mechanical_piston/piston_pole", PistonScenes::poles);
		HELPER.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.addStoryBoard("mechanical_piston/modes", PistonScenes::movementModes);

		// Windmill Bearing
		HELPER.forComponents(AllBlocks.ROPE_PULLEY)
			.addStoryBoard("rope_pulley/anchor", PulleyScenes::movement, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR)
			.addStoryBoard("rope_pulley/modes", PulleyScenes::movementModes)
			.addStoryBoard("rope_pulley/attachment", PulleyScenes::attachment);

		// Windmill Bearing
		HELPER.forComponents(AllBlocks.WINDMILL_BEARING)
			.addStoryBoard("windmill_bearing/source", BearingScenes::windmillsAsSource, PonderTag.KINETIC_SOURCES)
			.addStoryBoard("windmill_bearing/structure", BearingScenes::windmillsAnyStructure,
				PonderTag.MOVEMENT_ANCHOR);
		HELPER.forComponents(AllBlocks.SAIL)
			.addStoryBoard("sail", BearingScenes::sail);
		HELPER.forComponents(AllBlocks.SAIL_FRAME)
			.addStoryBoard("sail", BearingScenes::sailFrame);

		// Mechanical Bearing
		HELPER.forComponents(AllBlocks.MECHANICAL_BEARING)
			.addStoryBoard("mechanical_bearing/anchor", BearingScenes::mechanicalBearing, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR)
			.addStoryBoard("mechanical_bearing/modes", BearingScenes::bearingModes)
			.addStoryBoard("mechanical_bearing/stabilized", BearingScenes::stabilizedBearings,
				PonderTag.CONTRAPTION_ACTOR);

		// Clockwork Bearing
		HELPER.addStoryBoard(AllBlocks.CLOCKWORK_BEARING, "clockwork_bearing", BearingScenes::clockwork,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);

		// Gantries
		HELPER.addStoryBoard(AllBlocks.GANTRY_SHAFT, "gantry/intro", GantryScenes::introForShaft,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);
		HELPER.addStoryBoard(AllBlocks.GANTRY_CARRIAGE, "gantry/intro", GantryScenes::introForPinion,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);
		HELPER.forComponents(AllBlocks.GANTRY_SHAFT, AllBlocks.GANTRY_CARRIAGE)
			.addStoryBoard("gantry/redstone", GantryScenes::redstone)
			.addStoryBoard("gantry/direction", GantryScenes::direction)
			.addStoryBoard("gantry/subgantry", GantryScenes::subgantry);

		// Cart Assembler
		HELPER.forComponents(AllBlocks.CART_ASSEMBLER)
			.addStoryBoard("cart_assembler/anchor", CartAssemblerScenes::anchor, PonderTag.MOVEMENT_ANCHOR)
			.addStoryBoard("cart_assembler/modes", CartAssemblerScenes::modes)
			.addStoryBoard("cart_assembler/dual", CartAssemblerScenes::dual)
			.addStoryBoard("cart_assembler/rails", CartAssemblerScenes::rails);

		// Movement Actors
		HELPER.forComponents(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.addStoryBoard("portable_interface/transfer", MovementActorScenes::psiTransfer, PonderTag.CONTRAPTION_ACTOR)
			.addStoryBoard("portable_interface/redstone", MovementActorScenes::psiRedstone);
		HELPER.forComponents(AllBlocks.REDSTONE_CONTACT)
			.addStoryBoard("redstone_contact", RedstoneScenes::contact);
		HELPER.forComponents(AllBlocks.MECHANICAL_SAW)
			.addStoryBoard("mechanical_saw/processing", MechanicalSawScenes::processing, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("mechanical_saw/breaker", MechanicalSawScenes::treeCutting)
			.addStoryBoard("mechanical_saw/contraption", MechanicalSawScenes::contraption, PonderTag.CONTRAPTION_ACTOR);
		HELPER.forComponents(AllBlocks.MECHANICAL_DRILL)
			.addStoryBoard("mechanical_drill/breaker", MechanicalDrillScenes::breaker, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("mechanical_drill/contraption", MechanicalDrillScenes::contraption,
				PonderTag.CONTRAPTION_ACTOR);
		HELPER.forComponents(AllBlocks.DEPLOYER)
			.addStoryBoard("deployer/filter", DeployerScenes::filter, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("deployer/modes", DeployerScenes::modes)
			.addStoryBoard("deployer/processing", DeployerScenes::processing)
			.addStoryBoard("deployer/redstone", DeployerScenes::redstone)
			.addStoryBoard("deployer/contraption", DeployerScenes::contraption, PonderTag.CONTRAPTION_ACTOR);
		HELPER.forComponents(AllBlocks.MECHANICAL_HARVESTER)
			.addStoryBoard("harvester", MovementActorScenes::harvester);
		HELPER.forComponents(AllBlocks.MECHANICAL_PLOUGH)
			.addStoryBoard("plough", MovementActorScenes::plough);

		// Fluids
		HELPER.forComponents(AllBlocks.FLUID_PIPE)
			.addStoryBoard("fluid_pipe/flow", PipeScenes::flow, PonderTag.FLUIDS)
			.addStoryBoard("fluid_pipe/interaction", PipeScenes::interaction)
			.addStoryBoard("fluid_pipe/encasing", PipeScenes::encasing);
		HELPER.forComponents(AllBlocks.COPPER_CASING)
			.addStoryBoard("fluid_pipe/encasing", PipeScenes::encasing);
		HELPER.forComponents(AllBlocks.MECHANICAL_PUMP)
			.addStoryBoard("mechanical_pump/flow", PumpScenes::flow, PonderTag.FLUIDS, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("mechanical_pump/speed", PumpScenes::speed);
		HELPER.forComponents(AllBlocks.FLUID_VALVE)
			.addStoryBoard("fluid_valve", PipeScenes::valve, PonderTag.FLUIDS, PonderTag.KINETIC_APPLIANCES);
		HELPER.forComponents(AllBlocks.SMART_FLUID_PIPE)
			.addStoryBoard("smart_pipe", PipeScenes::smart, PonderTag.FLUIDS);
		HELPER.forComponents(AllBlocks.FLUID_TANK)
			.addStoryBoard("fluid_tank/storage", FluidTankScenes::storage, PonderTag.FLUIDS)
			.addStoryBoard("fluid_tank/sizes", FluidTankScenes::sizes);
		HELPER.forComponents(AllBlocks.CREATIVE_FLUID_TANK)
			.addStoryBoard("fluid_tank/storage_creative", FluidTankScenes::creative, PonderTag.FLUIDS,
				PonderTag.CREATIVE)
			.addStoryBoard("fluid_tank/sizes_creative", FluidTankScenes::sizes);
		HELPER.forComponents(AllBlocks.HOSE_PULLEY)
			.addStoryBoard("hose_pulley/intro", HosePulleyScenes::intro, PonderTag.FLUIDS, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("hose_pulley/level", HosePulleyScenes::level)
			.addStoryBoard("hose_pulley/infinite", HosePulleyScenes::infinite);
		HELPER.forComponents(AllBlocks.SPOUT)
			.addStoryBoard("spout", SpoutScenes::filling, PonderTag.FLUIDS);
		HELPER.forComponents(AllBlocks.ITEM_DRAIN)
			.addStoryBoard("item_drain", DrainScenes::emptying, PonderTag.FLUIDS);
		HELPER.forComponents(AllBlocks.PORTABLE_FLUID_INTERFACE)
			.addStoryBoard("portable_interface/transfer_fluid", FluidMovementActorScenes::transfer, PonderTag.FLUIDS,
				PonderTag.CONTRAPTION_ACTOR)
			.addStoryBoard("portable_interface/redstone_fluid", MovementActorScenes::psiRedstone);

		// Redstone
		HELPER.forComponents(AllBlocks.PULSE_EXTENDER)
			.addStoryBoard("pulse_extender", RedstoneScenes::pulseExtender);
		HELPER.forComponents(AllBlocks.PULSE_REPEATER)
			.addStoryBoard("pulse_repeater", RedstoneScenes::pulseRepeater);
		HELPER.forComponents(AllBlocks.POWERED_LATCH)
			.addStoryBoard("powered_latch", RedstoneScenes::poweredLatch);
		HELPER.forComponents(AllBlocks.POWERED_TOGGLE_LATCH)
			.addStoryBoard("powered_toggle_latch", RedstoneScenes::poweredToggleLatch);
		HELPER.forComponents(AllBlocks.ANALOG_LEVER)
			.addStoryBoard("analog_lever", RedstoneScenes::analogLever);
		HELPER.forComponents(AllBlocks.ORANGE_NIXIE_TUBE)
			.addStoryBoard("nixie_tube", RedstoneScenes::nixieTube);
		HELPER.forComponents(AllBlocks.REDSTONE_LINK)
			.addStoryBoard("redstone_link", RedstoneScenes::redstoneLink);
		HELPER.forComponents(AllBlocks.ROSE_QUARTZ_LAMP)
			.addStoryBoard("rose_quartz_lamp", RedstoneScenes2::roseQuartzLamp);

		// Trains
		HELPER.forComponents(TrackMaterial.allBlocks().stream()
						.map((trackSupplier) -> new BlockEntry<TrackBlock>(
			// note: these blocks probably WON'T be in the Create Registrate, but a simple code trace reveals the Entry's registrate isn't used
								Create.REGISTRATE,
								RegistryObject.create(trackSupplier.get().getRegistryName(), ForgeRegistries.BLOCKS)))
						.toList())
			.addStoryBoard("train_track/placement", TrackScenes::placement)
			.addStoryBoard("train_track/portal", TrackScenes::portal)
			.addStoryBoard("train_track/chunks", TrackScenes::chunks);

		HELPER.forComponents(AllBlocks.TRACK_STATION)
			.addStoryBoard("train_station/assembly", TrainStationScenes::assembly)
			.addStoryBoard("train_station/schedule", TrainStationScenes::autoSchedule);

		HELPER.forComponents(AllBlocks.TRACK_SIGNAL)
			.addStoryBoard("train_signal/placement", TrainSignalScenes::placement)
			.addStoryBoard("train_signal/signaling", TrainSignalScenes::signaling)
			.addStoryBoard("train_signal/redstone", TrainSignalScenes::redstone);

		HELPER.forComponents(AllItems.SCHEDULE)
			.addStoryBoard("train_schedule", TrainScenes::schedule);

		HELPER.forComponents(AllBlocks.CONTROLS)
			.addStoryBoard("train_controls", TrainScenes::controls);

		HELPER.forComponents(AllBlocks.TRACK_OBSERVER)
			.addStoryBoard("train_observer", TrackObserverScenes::observe);

		// Display Link
		HELPER.forComponents(AllBlocks.DISPLAY_LINK)
			.addStoryBoard("display_link", DisplayScenes::link)
			.addStoryBoard("display_link_redstone", DisplayScenes::redstone);
		HELPER.forComponents(AllBlocks.DISPLAY_BOARD)
			.addStoryBoard("display_board", DisplayScenes::board);

		// Steam
		HELPER.forComponents(AllBlocks.STEAM_WHISTLE)
			.addStoryBoard("steam_whistle", SteamScenes::whistle);
		HELPER.forComponents(AllBlocks.STEAM_ENGINE)
			.addStoryBoard("steam_engine", SteamScenes::engine);

		// Debug scenes, can be found in game via the Brass Hand
		if (REGISTER_DEBUG_SCENES)
			DebugScenes.registerAll();
	}

	public static boolean editingModeActive() {
		return AllConfigs.CLIENT.editingMode.get();
	}

	public static void registerTags() {
		// Add items to tags here

		PonderRegistry.TAGS.forTag(PonderTag.KINETIC_RELAYS)
			.add(AllBlocks.SHAFT)
			.add(AllBlocks.COGWHEEL)
			.add(AllBlocks.LARGE_COGWHEEL)
			.add(AllItems.BELT_CONNECTOR)
			.add(AllBlocks.GEARBOX)
			.add(AllBlocks.CLUTCH)
			.add(AllBlocks.GEARSHIFT)
			.add(AllBlocks.ENCASED_CHAIN_DRIVE)
			.add(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
			.add(AllBlocks.SEQUENCED_GEARSHIFT)
			.add(AllBlocks.ROTATION_SPEED_CONTROLLER);

		PonderRegistry.TAGS.forTag(PonderTag.KINETIC_SOURCES)
			.add(AllBlocks.HAND_CRANK)
			.add(AllBlocks.COPPER_VALVE_HANDLE)
			.add(AllBlocks.WATER_WHEEL)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.STEAM_ENGINE)
			.add(AllBlocks.CREATIVE_MOTOR);

		PonderRegistry.TAGS.forTag(PonderTag.TRAIN_RELATED)
			.add(AllBlocks.TRACK)
			.add(AllBlocks.TRACK_STATION)
			.add(AllBlocks.TRACK_SIGNAL)
			.add(AllBlocks.TRACK_OBSERVER)
			.add(AllBlocks.CONTROLS)
			.add(AllItems.SCHEDULE)
			.add(AllBlocks.TRAIN_DOOR)
			.add(AllBlocks.TRAIN_TRAPDOOR)
			.add(AllBlocks.RAILWAY_CASING);

		PonderRegistry.TAGS.forTag(PonderTag.KINETIC_APPLIANCES)
			.add(AllBlocks.MILLSTONE)
			.add(AllBlocks.TURNTABLE)
			.add(AllBlocks.ENCASED_FAN)
			.add(AllBlocks.CUCKOO_CLOCK)
			.add(AllBlocks.MECHANICAL_PRESS)
			.add(AllBlocks.MECHANICAL_MIXER)
			.add(AllBlocks.MECHANICAL_CRAFTER)
			.add(AllBlocks.MECHANICAL_DRILL)
			.add(AllBlocks.MECHANICAL_SAW)
			.add(AllBlocks.DEPLOYER)
			.add(AllBlocks.MECHANICAL_PUMP)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllBlocks.MECHANICAL_PISTON)
			.add(AllBlocks.ROPE_PULLEY)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.GANTRY_SHAFT)
			.add(AllBlocks.GANTRY_CARRIAGE)
			.add(AllBlocks.CLOCKWORK_BEARING)
			.add(AllBlocks.DISPLAY_BOARD)
			.add(AllBlocks.CRUSHING_WHEEL);

		PonderRegistry.TAGS.forTag(PonderTag.FLUIDS)
			.add(AllBlocks.FLUID_PIPE)
			.add(AllBlocks.MECHANICAL_PUMP)
			.add(AllBlocks.FLUID_VALVE)
			.add(AllBlocks.SMART_FLUID_PIPE)
			.add(AllBlocks.HOSE_PULLEY)
			.add(AllBlocks.ITEM_DRAIN)
			.add(AllBlocks.SPOUT)
			.add(AllBlocks.PORTABLE_FLUID_INTERFACE)
			.add(AllBlocks.FLUID_TANK)
			.add(AllBlocks.CREATIVE_FLUID_TANK);

		PonderRegistry.TAGS.forTag(PonderTag.ARM_TARGETS)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllItems.BELT_CONNECTOR)
			.add(AllBlocks.CHUTE)
			.add(AllBlocks.DEPOT)
			.add(AllBlocks.WEIGHTED_EJECTOR)
			.add(AllBlocks.BASIN)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.MECHANICAL_CRAFTER)
			.add(AllBlocks.MILLSTONE)
			.add(AllBlocks.DEPLOYER)
			.add(AllBlocks.MECHANICAL_SAW)
			.add(AllBlocks.BLAZE_BURNER)
			.add(AllBlocks.CRUSHING_WHEEL)
			.add(AllBlocks.TRACK_STATION)
			.add(Blocks.COMPOSTER)
			.add(Blocks.JUKEBOX)
			.add(Blocks.CAMPFIRE)
			.add(Blocks.SOUL_CAMPFIRE)
			.add(Blocks.RESPAWN_ANCHOR);

		PonderRegistry.TAGS.forTag(PonderTag.LOGISTICS)
			.add(AllItems.BELT_CONNECTOR)
			.add(AllItems.FILTER)
			.add(AllItems.ATTRIBUTE_FILTER)
			.add(AllBlocks.CHUTE)
			.add(AllBlocks.SMART_CHUTE)
			.add(AllBlocks.ITEM_VAULT)
			.add(AllBlocks.DEPOT)
			.add(AllBlocks.WEIGHTED_EJECTOR)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.ANDESITE_TUNNEL)
			.add(AllBlocks.BRASS_TUNNEL)
			.add(AllBlocks.CONTENT_OBSERVER)
			.add(AllBlocks.STOCKPILE_SWITCH)
			.add(AllBlocks.CREATIVE_CRATE)
			.add(AllBlocks.PORTABLE_STORAGE_INTERFACE);

		PonderRegistry.TAGS.forTag(PonderTag.DECORATION)
			.add(AllBlocks.ORANGE_NIXIE_TUBE)
			.add(AllBlocks.DISPLAY_BOARD)
			.add(AllBlocks.CUCKOO_CLOCK)
			.add(AllBlocks.WOODEN_BRACKET)
			.add(AllBlocks.METAL_BRACKET)
			.add(AllBlocks.METAL_GIRDER)
			.add(AllBlocks.ANDESITE_CASING)
			.add(AllBlocks.BRASS_CASING)
			.add(AllBlocks.COPPER_CASING)
			.add(AllBlocks.RAILWAY_CASING);

		PonderRegistry.TAGS.forTag(PonderTag.CREATIVE)
			.add(AllBlocks.CREATIVE_CRATE)
			.add(AllBlocks.CREATIVE_FLUID_TANK)
			.add(AllBlocks.CREATIVE_MOTOR);

		PonderRegistry.TAGS.forTag(PonderTag.SAILS)
			.add(AllBlocks.SAIL)
			.add(AllBlocks.SAIL_FRAME)
			.add(Blocks.WHITE_WOOL);

		PonderRegistry.TAGS.forTag(PonderTag.REDSTONE)
			.add(AllBlocks.ORANGE_NIXIE_TUBE)
			.add(AllBlocks.REDSTONE_CONTACT)
			.add(AllBlocks.ANALOG_LEVER)
			.add(AllBlocks.REDSTONE_LINK)
			.add(AllBlocks.PULSE_EXTENDER)
			.add(AllBlocks.PULSE_REPEATER)
			.add(AllBlocks.POWERED_LATCH)
			.add(AllBlocks.POWERED_TOGGLE_LATCH)
			.add(AllBlocks.ROSE_QUARTZ_LAMP);

		PonderRegistry.TAGS.forTag(PonderTag.MOVEMENT_ANCHOR)
			.add(AllBlocks.MECHANICAL_PISTON)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.CLOCKWORK_BEARING)
			.add(AllBlocks.ROPE_PULLEY)
			.add(AllBlocks.GANTRY_CARRIAGE)
			.add(AllBlocks.CART_ASSEMBLER)
			.add(AllBlocks.TRACK_STATION);

		PonderRegistry.TAGS.forTag(PonderTag.CONTRAPTION_ASSEMBLY)
			.add(AllBlocks.LINEAR_CHASSIS)
			.add(AllBlocks.SECONDARY_LINEAR_CHASSIS)
			.add(AllBlocks.RADIAL_CHASSIS)
			.add(AllItems.SUPER_GLUE)
			.add(AllBlocks.STICKER)
			.add(Blocks.SLIME_BLOCK)
			.add(Blocks.HONEY_BLOCK);

		PonderRegistry.TAGS.forTag(PonderTag.CONTRAPTION_ACTOR)
			.add(AllBlocks.MECHANICAL_HARVESTER)
			.add(AllBlocks.MECHANICAL_PLOUGH)
			.add(AllBlocks.MECHANICAL_DRILL)
			.add(AllBlocks.MECHANICAL_SAW)
			.add(AllBlocks.DEPLOYER)
			.add(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.add(AllBlocks.PORTABLE_FLUID_INTERFACE)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.SEATS.get(DyeColor.WHITE))
			.add(AllBlocks.CONTROLS)
			.add(AllBlocks.REDSTONE_CONTACT)
			.add(Blocks.BELL)
			.add(Blocks.DISPENSER)
			.add(Blocks.DROPPER);

		PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_SOURCES)
			.add(AllBlocks.SEATS.get(DyeColor.WHITE))
			.add(AllBlocks.ORANGE_NIXIE_TUBE)
			.add(AllBlocks.STOCKPILE_SWITCH)
			.add(AllBlocks.CONTENT_OBSERVER)
			.add(AllBlocks.ANDESITE_TUNNEL)
			.add(AllBlocks.TRACK_OBSERVER)
			.add(AllBlocks.TRACK_STATION)
			.add(AllBlocks.DISPLAY_LINK)
			.add(AllBlocks.BRASS_TUNNEL)
			.add(AllBlocks.CUCKOO_CLOCK)
			.add(AllBlocks.STRESSOMETER)
			.add(AllBlocks.SPEEDOMETER)
			.add(AllBlocks.FLUID_TANK)
			.add(AllItems.BELT_CONNECTOR)
			.add(Blocks.ENCHANTING_TABLE)
			.add(Blocks.RESPAWN_ANCHOR)
			.add(Blocks.COMMAND_BLOCK)
			.add(Blocks.TARGET);

		PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_TARGETS)
			.add(AllBlocks.ORANGE_NIXIE_TUBE)
			.add(AllBlocks.DISPLAY_BOARD)
			.add(AllBlocks.DISPLAY_LINK)
			.add(Blocks.OAK_SIGN)
			.add(Blocks.LECTERN);

	}

}
