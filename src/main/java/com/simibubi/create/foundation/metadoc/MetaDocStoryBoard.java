package com.simibubi.create.foundation.metadoc;

import com.simibubi.create.foundation.metadoc.MetaDocScene.SceneBuilder;

import net.minecraft.util.math.Vec3i;

public abstract class MetaDocStoryBoard {

	public abstract String getSchematicName();
	
	public abstract String getStoryTitle();

	public abstract void program(SceneBuilder scene, Vec3i worldSize);

}
