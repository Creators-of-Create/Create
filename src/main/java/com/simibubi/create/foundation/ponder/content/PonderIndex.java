package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.PonderRegistry;

import net.minecraft.block.Blocks;

public class PonderIndex {

	public static final boolean EDITOR_MODE = true;

	public static void register() {
		// Register storyboards here
		// (!) Added entries require re-launch
		// (!) Modifications inside storyboard methods only require re-opening the ui

		PonderRegistry.forComponents(AllBlocks.SHAFT)
			.addStoryBoard("shaft/relay", KineticsScenes::shaftAsRelay, PonderTag.KINETIC_RELAYS);
		PonderRegistry.forComponents(AllBlocks.SHAFT, AllBlocks.ANDESITE_ENCASED_SHAFT, AllBlocks.BRASS_ENCASED_SHAFT)
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased);

		PonderRegistry.forComponents(AllBlocks.COGWHEEL)
			.addStoryBoard("cog/small", KineticsScenes::cogAsRelay, PonderTag.KINETIC_RELAYS)
			.addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp);

		PonderRegistry.forComponents(AllBlocks.LARGE_COGWHEEL)
			.addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp)
			.addStoryBoard("cog/large", KineticsScenes::largeCogAsRelay, PonderTag.KINETIC_RELAYS);

		PonderRegistry.forComponents(AllItems.BELT_CONNECTOR)
			.addStoryBoard("belt/connect", BeltScenes::beltConnector, PonderTag.KINETIC_RELAYS)
			.addStoryBoard("belt/directions", BeltScenes::directions)
			.addStoryBoard("belt/transport", BeltScenes::transport, PonderTag.LOGISTICS)
			.addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

		PonderRegistry.forComponents(AllBlocks.ANDESITE_CASING, AllBlocks.BRASS_CASING)
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased)
			.addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

		PonderRegistry.forComponents(AllBlocks.GEARBOX, AllItems.VERTICAL_GEARBOX)
			.addStoryBoard("gearbox", KineticsScenes::gearbox, PonderTag.KINETIC_RELAYS);

		PonderRegistry.addStoryBoard(AllBlocks.CLUTCH, "clutch", KineticsScenes::clutch, PonderTag.KINETIC_RELAYS);
		PonderRegistry.addStoryBoard(AllBlocks.GEARSHIFT, "gearshift", KineticsScenes::gearshift,
			PonderTag.KINETIC_RELAYS);

		PonderRegistry.forComponents(AllBlocks.ENCASED_FAN)
			.addStoryBoard("fan/direction", FanScenes::direction, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("fan/processing", FanScenes::processing)
			.addStoryBoard("fan/source", FanScenes::source, PonderTag.KINETIC_SOURCES);

		PonderRegistry.addStoryBoard(AllBlocks.CREATIVE_MOTOR, "creative_motor", KineticsScenes::creativeMotor,
			PonderTag.KINETIC_SOURCES);
		PonderRegistry.addStoryBoard(AllBlocks.WATER_WHEEL, "water_wheel", KineticsScenes::waterWheel,
			PonderTag.KINETIC_SOURCES);
		PonderRegistry.addStoryBoard(AllBlocks.HAND_CRANK, "hand_crank", KineticsScenes::handCrank,
			PonderTag.KINETIC_SOURCES);

		PonderRegistry.addStoryBoard(AllBlocks.COPPER_VALVE_HANDLE, "valve_handle", KineticsScenes::valveHandle,
			PonderTag.KINETIC_SOURCES);
		PonderRegistry.forComponents(AllBlocks.DYED_VALVE_HANDLES)
			.addStoryBoard("valve_handle", KineticsScenes::valveHandle);

		PonderRegistry.addStoryBoard(AllBlocks.ENCASED_CHAIN_DRIVE, "chain_drive/relay",
			ChainDriveScenes::chainDriveAsRelay, PonderTag.KINETIC_RELAYS);
		PonderRegistry.forComponents(AllBlocks.ENCASED_CHAIN_DRIVE, AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT)
			.addStoryBoard("chain_drive/gearshift", ChainDriveScenes::adjustableChainGearshift);

		// Funnels
		PonderRegistry.addStoryBoard(AllBlocks.BRASS_FUNNEL, "funnels/brass", FunnelScenes::brass);
		PonderRegistry.forComponents(AllBlocks.ANDESITE_FUNNEL, AllBlocks.BRASS_FUNNEL)
			.addStoryBoard("funnels/intro", FunnelScenes::intro, PonderTag.LOGISTICS)
			.addStoryBoard("funnels/direction", FunnelScenes::directionality)
			.addStoryBoard("funnels/compat", FunnelScenes::compat)
			.addStoryBoard("funnels/redstone", FunnelScenes::redstone)
			.addStoryBoard("funnels/transposer", FunnelScenes::transposer);
		PonderRegistry.addStoryBoard(AllBlocks.ANDESITE_FUNNEL, "funnels/brass", FunnelScenes::brass);

		// Chassis & Super Glue
		PonderRegistry.forComponents(AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)
			.addStoryBoard("chassis/linear_group", ChassisScenes::linearGroup, PonderTag.CONTRAPTION_ASSEMBLY)
			.addStoryBoard("chassis/linear_attachment", ChassisScenes::linearAttachement);
		PonderRegistry.forComponents(AllBlocks.RADIAL_CHASSIS)
			.addStoryBoard("chassis/radial", ChassisScenes::radial, PonderTag.CONTRAPTION_ASSEMBLY);
		PonderRegistry.forComponents(AllItems.SUPER_GLUE)
			.addStoryBoard("super_glue", ChassisScenes::superGlue, PonderTag.CONTRAPTION_ASSEMBLY);
		PonderRegistry.forComponents(AllBlocks.STICKER)
			.addStoryBoard("sticker", RedstoneScenes::sticker, PonderTag.CONTRAPTION_ASSEMBLY);

		// Mechanical Piston
		PonderRegistry.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.addStoryBoard("mechanical_piston/anchor", PistonScenes::movement, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR);
		PonderRegistry
			.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON,
				AllBlocks.PISTON_EXTENSION_POLE)
			.addStoryBoard("mechanical_piston/piston_pole", PistonScenes::poles);
		PonderRegistry.forComponents(AllBlocks.MECHANICAL_PISTON, AllBlocks.STICKY_MECHANICAL_PISTON)
			.addStoryBoard("mechanical_piston/modes", PistonScenes::movementModes);

		// Windmill Bearing
		PonderRegistry.forComponents(AllBlocks.ROPE_PULLEY)
			.addStoryBoard("rope_pulley/anchor", PulleyScenes::movement, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR)
			.addStoryBoard("rope_pulley/modes", PulleyScenes::movementModes)
			.addStoryBoard("rope_pulley/attachment", PulleyScenes::attachment);

		// Windmill Bearing
		PonderRegistry.forComponents(AllBlocks.WINDMILL_BEARING)
			.addStoryBoard("windmill_bearing/source", BearingScenes::windmillsAsSource, PonderTag.KINETIC_SOURCES)
			.addStoryBoard("windmill_bearing/structure", BearingScenes::windmillsAnyStructure,
				PonderTag.MOVEMENT_ANCHOR);

		// Mechanical Bearing
		PonderRegistry.forComponents(AllBlocks.MECHANICAL_BEARING)
			.addStoryBoard("mechanical_bearing/anchor", BearingScenes::mechanicalBearing, PonderTag.KINETIC_APPLIANCES,
				PonderTag.MOVEMENT_ANCHOR)
			.addStoryBoard("mechanical_bearing/modes", BearingScenes::bearingModes)
			.addStoryBoard("mechanical_bearing/stabilized", BearingScenes::stabilizedBearings,
				PonderTag.CONTRAPTION_ACTOR);

		// Clockwork Bearing
		PonderRegistry.addStoryBoard(AllBlocks.CLOCKWORK_BEARING, "clockwork_bearing", BearingScenes::clockwork,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);

		// Gantries
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_SHAFT, "gantry/intro", GantryScenes::introForShaft,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_CARRIAGE, "gantry/intro", GantryScenes::introForPinion,
			PonderTag.KINETIC_APPLIANCES, PonderTag.MOVEMENT_ANCHOR);
		PonderRegistry.forComponents(AllBlocks.GANTRY_SHAFT, AllBlocks.GANTRY_CARRIAGE)
			.addStoryBoard("gantry/redstone", GantryScenes::redstone)
			.addStoryBoard("gantry/direction", GantryScenes::direction)
			.addStoryBoard("gantry/subgantry", GantryScenes::subgantry);

		// Movement Actors
		PonderRegistry.forComponents(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.addStoryBoard("portable_interface/transfer", MovementActorScenes::psiTransfer, PonderTag.CONTRAPTION_ACTOR)
			.addStoryBoard("portable_interface/redstone", MovementActorScenes::psiRedstone);
		PonderRegistry.forComponents(AllBlocks.REDSTONE_CONTACT)
			.addStoryBoard("redstone_contact", RedstoneScenes::contact);
		PonderRegistry.forComponents(AllBlocks.MECHANICAL_SAW)
			.addStoryBoard("mechanical_saw/processing", MechanicalSawScenes::processing, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("mechanical_saw/breaker", MechanicalSawScenes::treeCutting)
			.addStoryBoard("mechanical_saw/contraption", MechanicalSawScenes::contraption, PonderTag.CONTRAPTION_ACTOR);
		PonderRegistry.forComponents(AllBlocks.MECHANICAL_DRILL)
			.addStoryBoard("mechanical_drill/breaker", MechanicalDrillScenes::breaker, PonderTag.KINETIC_APPLIANCES)
			.addStoryBoard("mechanical_drill/contraption", MechanicalDrillScenes::contraption,
				PonderTag.CONTRAPTION_ACTOR);

		// Redstone
		PonderRegistry.forComponents(AllBlocks.PULSE_REPEATER)
			.addStoryBoard("pulse_repeater", RedstoneScenes::pulseRepeater);
		PonderRegistry.forComponents(AllBlocks.ADJUSTABLE_REPEATER)
			.addStoryBoard("adjustable_repeater", RedstoneScenes::adjustableRepeater);
		PonderRegistry.forComponents(AllBlocks.ADJUSTABLE_PULSE_REPEATER)
			.addStoryBoard("adjustable_pulse_repeater", RedstoneScenes::adjustablePulseRepeater);
		PonderRegistry.forComponents(AllBlocks.POWERED_LATCH)
			.addStoryBoard("powered_latch", RedstoneScenes::poweredLatch);
		PonderRegistry.forComponents(AllBlocks.POWERED_TOGGLE_LATCH)
			.addStoryBoard("powered_toggle_latch", RedstoneScenes::poweredToggleLatch);
		PonderRegistry.forComponents(AllBlocks.ANALOG_LEVER)
			.addStoryBoard("analog_lever", RedstoneScenes::analogLever);
		PonderRegistry.forComponents(AllBlocks.NIXIE_TUBE)
			.addStoryBoard("nixie_tube", RedstoneScenes::nixieTube);
		PonderRegistry.forComponents(AllBlocks.REDSTONE_LINK)
			.addStoryBoard("redstone_link", RedstoneScenes::redstoneLink);

		// Debug scenes, can be found in game via the Brass Hand
		if (EDITOR_MODE)
			DebugScenes.registerAll();
	}

	public static void registerTags() {
		// Add items to tags here

		PonderRegistry.tags.forTag(PonderTag.KINETIC_RELAYS)
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

		PonderRegistry.tags.forTag(PonderTag.KINETIC_SOURCES)
			.add(AllBlocks.HAND_CRANK)
			.add(AllBlocks.COPPER_VALVE_HANDLE)
			.add(AllBlocks.WATER_WHEEL)
			.add(AllBlocks.ENCASED_FAN)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.FURNACE_ENGINE)
			.add(AllBlocks.FLYWHEEL)
			.add(AllBlocks.CREATIVE_MOTOR);

		PonderRegistry.tags.forTag(PonderTag.KINETIC_APPLIANCES)
			.add(AllBlocks.MILLSTONE)
			.add(AllBlocks.TURNTABLE)
			.add(AllBlocks.ENCASED_FAN)
			.add(AllBlocks.CUCKOO_CLOCK)
			.add(AllBlocks.MECHANICAL_PRESS)
			.add(AllBlocks.MECHANICAL_MIXER)
			.add(AllBlocks.MECHANICAL_CRAFTER)
			.add(AllBlocks.MECHANICAL_DRILL)
			.add(AllBlocks.MECHANICAL_SAW)
			.add(AllBlocks.MECHANICAL_PUMP)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllBlocks.MECHANICAL_PISTON)
			.add(AllBlocks.ROPE_PULLEY)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.GANTRY_SHAFT)
			.add(AllBlocks.GANTRY_CARRIAGE)
			.add(AllBlocks.CLOCKWORK_BEARING)
			.add(AllBlocks.CRUSHING_WHEEL);

		PonderRegistry.tags.forTag(PonderTag.FLUIDS)
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

		PonderRegistry.tags.forTag(PonderTag.ARM_TARGETS)
			.add(AllItems.BELT_CONNECTOR)
			.add(AllBlocks.CHUTE)
			.add(AllBlocks.DEPOT)
			.add(AllBlocks.BASIN)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.MECHANICAL_CRAFTER)
			.add(AllBlocks.MILLSTONE)
			.add(AllBlocks.DEPLOYER)
			.add(AllBlocks.MECHANICAL_SAW)
			.add(Blocks.COMPOSTER)
			.add(AllBlocks.BLAZE_BURNER)
			.add(Blocks.JUKEBOX)
			.add(AllBlocks.CRUSHING_WHEEL);

		PonderRegistry.tags.forTag(PonderTag.LOGISTICS)
			.add(AllItems.BELT_CONNECTOR)
			.add(AllItems.FILTER)
			.add(AllItems.ATTRIBUTE_FILTER)
			.add(AllBlocks.CHUTE)
			.add(AllBlocks.SMART_CHUTE)
			.add(AllBlocks.DEPOT)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.ANDESITE_TUNNEL)
			.add(AllBlocks.BRASS_TUNNEL)
			.add(AllBlocks.CONTENT_OBSERVER)
			.add(AllBlocks.STOCKPILE_SWITCH)
			.add(AllBlocks.ADJUSTABLE_CRATE)
			.add(AllBlocks.CREATIVE_CRATE)
			.add(AllBlocks.PORTABLE_STORAGE_INTERFACE);

		PonderRegistry.tags.forTag(PonderTag.DECORATION)
			.add(AllBlocks.NIXIE_TUBE)
			.add(AllBlocks.CUCKOO_CLOCK)
			.add(AllBlocks.WOODEN_BRACKET)
			.add(AllBlocks.METAL_BRACKET)
			.add(AllBlocks.ANDESITE_CASING)
			.add(AllBlocks.BRASS_CASING)
			.add(AllBlocks.COPPER_CASING);

		PonderRegistry.tags.forTag(PonderTag.CREATIVE)
			.add(AllBlocks.CREATIVE_CRATE)
			.add(AllBlocks.CREATIVE_FLUID_TANK)
			.add(AllBlocks.CREATIVE_MOTOR);

		PonderRegistry.tags.forTag(PonderTag.REDSTONE)
			.add(AllBlocks.NIXIE_TUBE)
			.add(AllBlocks.REDSTONE_CONTACT)
			.add(AllBlocks.ANALOG_LEVER)
			.add(AllBlocks.REDSTONE_LINK)
			.add(AllBlocks.ADJUSTABLE_REPEATER)
			.add(AllBlocks.PULSE_REPEATER)
			.add(AllBlocks.ADJUSTABLE_PULSE_REPEATER)
			.add(AllBlocks.POWERED_LATCH)
			.add(AllBlocks.POWERED_TOGGLE_LATCH);

		PonderRegistry.tags.forTag(PonderTag.MOVEMENT_ANCHOR)
			.add(AllBlocks.MECHANICAL_PISTON)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.CLOCKWORK_BEARING)
			.add(AllBlocks.ROPE_PULLEY)
			.add(AllBlocks.GANTRY_CARRIAGE)
			.add(AllBlocks.CART_ASSEMBLER);

		PonderRegistry.tags.forTag(PonderTag.CONTRAPTION_ASSEMBLY)
			.add(AllBlocks.LINEAR_CHASSIS)
			.add(AllBlocks.SECONDARY_LINEAR_CHASSIS)
			.add(AllBlocks.RADIAL_CHASSIS)
			.add(AllItems.SUPER_GLUE)
			.add(AllBlocks.STICKER)
			.add(Blocks.SLIME_BLOCK)
			.add(Blocks.field_226907_mc_); // honey block

		PonderRegistry.tags.forTag(PonderTag.CONTRAPTION_ACTOR)
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
			.add(AllBlocks.SEATS[0])
			.add(AllBlocks.REDSTONE_CONTACT)
			.add(AllBlocks.SAIL)
			.add(Blocks.BELL)
			.add(Blocks.DISPENSER)
			.add(Blocks.DROPPER);

	}

}
