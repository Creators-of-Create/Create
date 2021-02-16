package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.PonderRegistry;

public class PonderIndex {

	/**
	 * When true, lang files are bypassed and any text in ponder can be hot-swapped
	 * without the need of runData
	 */
	public static final boolean EDITOR_MODE = true;

	public static void register() {
		// Register storyboards here (Requires re-launch)

		PonderRegistry.addStoryBoard(AllBlocks.COGWHEEL, new CogwheelStory());
		PonderRegistry.addStoryBoard(AllBlocks.COGWHEEL, new CogwheelStory());
		PonderRegistry.addStoryBoard(AllBlocks.COGWHEEL, new CogwheelStory());
		
		PonderRegistry.addStoryBoard(AllBlocks.SHAFT, new ShaftAsRelay());
		PonderRegistry.addStoryBoard(AllBlocks.SHAFT, new ShaftsCanBeEncased());

		DebugScenes.registerAll();
	}

}
