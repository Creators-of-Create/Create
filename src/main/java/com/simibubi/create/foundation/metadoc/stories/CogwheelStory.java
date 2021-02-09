package com.simibubi.create.foundation.metadoc.stories;

import com.simibubi.create.foundation.metadoc.MetaDocScene.SceneBuilder;
import com.simibubi.create.foundation.metadoc.MetaDocStoryBoard;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class CogwheelStory extends MetaDocStoryBoard {

	@Override
	public String getSchematicName() {
		return "cogwheel/test";
	}

	@Override
	public void program(SceneBuilder scene, Vec3i worldSize) {
		scene.showBasePlate()
			.idle(5)
			.showSection(BlockPos.ZERO.up(), worldSize, Direction.DOWN);
	}

}
