package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CogwheelStory extends PonderStoryBoard {

	public CogwheelStory() {
	}

	@Override
	public String getSchematicName() {
		return "cogwheel/first";
	}

	@Override
	public String getStoryTitle() {
		return "My First Ponder Story, Parrots";
	}

	@Override
	public void program(SceneBuilder scene, SceneBuildingUtil util) {
		
		scene.movePOI(new Vec3d(3.5, 4, 4.5));
		scene.showBasePlate();
		scene.idle(10);

		scene.createParrotSpinningOn(new BlockPos(1, 4, 2), Direction.DOWN);
		scene.showSection(util.layersFrom(1), Direction.DOWN);
		
		scene.idle(10);
		scene.rotateCameraY(180);
		
	}

}
