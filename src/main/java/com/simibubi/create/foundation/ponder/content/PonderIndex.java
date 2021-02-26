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

		PonderRegistry.forComponents(AllBlocks.ANDESITE_FUNNEL, AllBlocks.BRASS_FUNNEL)
			.addStoryBoard("funnels/intro", FunnelScenes::intro)
			.addStoryBoard("funnels/direction", FunnelScenes::directionality)
			.addStoryBoard("funnels/mounting", FunnelScenes::mounting);
		// redstone
		// brass vs andesite
		// arm compat?
		
		// Debug scenes, can be found in game via the Brass Hand
		if (EDITOR_MODE)
			DebugScenes.registerAll();
	}

}
