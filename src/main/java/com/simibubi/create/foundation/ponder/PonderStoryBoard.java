package com.simibubi.create.foundation.ponder;

import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;

public abstract class PonderStoryBoard {

	public abstract String getSchematicName();
	
	public abstract String getStoryTitle();

	public abstract void program(SceneBuilder scene, SceneBuildingUtil util);

}
