package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.PonderRegistry;

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

		// Debug scenes, can be found in game via the Brass Hand
		if (EDITOR_MODE)
			DebugScenes.registerAll();
	}

}
