package com.simibubi.create.foundation.metadoc.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.metadoc.MetaDocs;

public class MetaDocIndex {
	
	/**
	 * When true, lang files are bypassed and any text in metadoc can be hot-swapped
	 * without the need of runData
	 */
	public static final boolean EDITOR_MODE = true;

	public static void register() {
		// Register storyboards here (Requires re-launch) 
	
		for (int i = 1; i < 6; i++)
			MetaDocs.addStoryBoard(AllBlocks.COGWHEEL, new CogwheelStory(i));
	
	}

}
