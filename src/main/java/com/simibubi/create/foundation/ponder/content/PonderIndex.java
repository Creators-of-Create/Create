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
			.addStoryBoard("shaft/relay", KineticsScenes::shaftAsRelay)
			.addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased);

		// Funnels
		PonderRegistry.addStoryBoard(AllBlocks.BRASS_FUNNEL, "funnels/brass", FunnelScenes::brass);
		PonderRegistry.forComponents(AllBlocks.ANDESITE_FUNNEL, AllBlocks.BRASS_FUNNEL)
			.addStoryBoard("funnels/intro", FunnelScenes::intro)
			.addStoryBoard("funnels/direction", FunnelScenes::directionality)
			.addStoryBoard("funnels/compat", FunnelScenes::compat)
			.addStoryBoard("funnels/redstone", FunnelScenes::redstone)
			.addStoryBoard("funnels/transposer", FunnelScenes::transposer);
		PonderRegistry.addStoryBoard(AllBlocks.ANDESITE_FUNNEL, "funnels/brass", FunnelScenes::brass);

		// Gantries
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_SHAFT, "gantry/intro", GantryScenes::introForShaft);
		PonderRegistry.addStoryBoard(AllBlocks.GANTRY_CARRIAGE, "gantry/intro", GantryScenes::introForPinion);
		PonderRegistry.forComponents(AllBlocks.GANTRY_SHAFT, AllBlocks.GANTRY_CARRIAGE)
			.addStoryBoard("gantry/redstone", GantryScenes::redstone)
			.addStoryBoard("gantry/direction", GantryScenes::direction)
			.addStoryBoard("gantry/subgantry", GantryScenes::subgantry);

		// Movement Actors
		PonderRegistry.forComponents(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.addStoryBoard("portable_interface/transfer", MovementActorScenes::psiTransfer)
			.addStoryBoard("portable_interface/redstone", MovementActorScenes::psiRedstone);

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
			.add(AllBlocks.ENCASED_CHAIN_DRIVE);

		PonderRegistry.tags.forTag(PonderTag.KINETIC_SOURCES)
			.add(AllBlocks.HAND_CRANK)
			.add(AllBlocks.COPPER_VALVE_HANDLE)
			.add(AllBlocks.WATER_WHEEL)
			.add(AllBlocks.ENCASED_FAN)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.FURNACE_ENGINE);

		PonderRegistry.tags.forTag(PonderTag.KINETIC_APPLIANCES)
			.add(AllBlocks.MILLSTONE)
			.add(AllBlocks.TURNTABLE)
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
			.add(AllBlocks.FLUID_TANK)
			.add(AllBlocks.ITEM_DRAIN)
			.add(AllBlocks.HOSE_PULLEY);

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
			.add(AllBlocks.CHUTE)
			.add(AllBlocks.SMART_CHUTE)
			.add(AllBlocks.DEPOT)
			.add(AllBlocks.MECHANICAL_ARM)
			.add(AllBlocks.ANDESITE_FUNNEL)
			.add(AllBlocks.BRASS_FUNNEL)
			.add(AllBlocks.ANDESITE_TUNNEL)
			.add(AllBlocks.BRASS_TUNNEL);

		PonderRegistry.tags.forTag(PonderTag.MOVEMENT_ANCHOR)
			.add(AllBlocks.MECHANICAL_PISTON)
			.add(AllBlocks.WINDMILL_BEARING)
			.add(AllBlocks.MECHANICAL_BEARING)
			.add(AllBlocks.CLOCKWORK_BEARING)
			.add(AllBlocks.ROPE_PULLEY)
			.add(AllBlocks.GANTRY_CARRIAGE)
			.add(AllBlocks.CART_ASSEMBLER);

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
			.add(Blocks.BELL)
			.add(Blocks.DISPENSER)
			.add(Blocks.DROPPER);

	}

}
