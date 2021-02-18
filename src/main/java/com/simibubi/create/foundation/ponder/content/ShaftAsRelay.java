package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

class ShaftAsRelay extends PonderStoryBoard {

	@Override
	public String getSchematicName() {
		return "shaft/shaft";
	}

	@Override
	public String getStoryTitle() {
		return "Relaying rotational force using Shafts";
	}

	@Override
	public void program(SceneBuilder scene, SceneBuildingUtil util) {
		scene.configureBasePlate(0, 0, 5);
		scene.showSection(util.layer(0), Direction.UP);

		Select gauge = Select.pos(0, 1, 2);
		scene.showSection(gauge, Direction.UP);
		scene.setKineticSpeed(gauge, 0);

		scene.idle(5);
		scene.showSection(Select.pos(5, 1, 2), Direction.DOWN);
		scene.idle(10);

		for (int i = 4; i >= 1; i--) {
			if (i == 2)
				scene.rotateCameraY(70);
			scene.idle(5);
			scene.showSection(Select.pos(i, 1, 2), Direction.DOWN);
		}

		scene.setKineticSpeed(gauge, 64);
		scene.indicateSuccess(new BlockPos(0, 1, 2));
		scene.idle(10);
		scene.showTargetedText(WHITE, new Vec3d(3, 1.5, 2.5), "shaft_relay",
			"Shafts will relay rotation in a straight line.", 1000);

		scene.idle(20);
		scene.markAsFinished();

	}

}
