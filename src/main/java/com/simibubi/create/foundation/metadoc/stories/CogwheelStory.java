package com.simibubi.create.foundation.metadoc.stories;

import com.simibubi.create.foundation.metadoc.MetaDocScene.SceneBuilder;
import com.simibubi.create.foundation.metadoc.MetaDocStoryBoard;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class CogwheelStory extends MetaDocStoryBoard {

	private int index;

	public CogwheelStory(int index) {
		this.index = index;
	}
	
	@Override
	public String getSchematicName() {
		return "cogwheel/s" + index;
	}

	@Override
	public void program(SceneBuilder scene, Vec3i worldSize) {
		scene.showBasePlate()
			.idle(10)
			.showSection(BlockPos.ZERO.up(), worldSize, Direction.DOWN);
	}

}
