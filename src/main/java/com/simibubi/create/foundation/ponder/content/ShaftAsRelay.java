package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

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
		scene.showBasePlate();

		Select encased = util.column(4, 2);
		Select gauge = util.column(0, 2);
		Select shafts = Select.cuboid(new BlockPos(1, 1, 2), new Vec3i(2, 0, 0));

		scene.idle(10);
		scene.showSection(encased, Direction.DOWN);
		scene.idle(10);
		scene.showSection(gauge, Direction.DOWN);
		scene.setKineticSpeed(gauge, 0);

		scene.idle(20);
		scene.showSection(shafts, Direction.DOWN);
		scene.setKineticSpeed(gauge, -112);

		scene.idle(10);
		scene.showTargetedText(WHITE, new Vec3d(3, 1.5, 2.5), "shaft_relay",
			"Shafts seem to relay rotation in a straight line.", 1000);
		
		scene.idle(20);
		scene.markAsFinished();

	}

}
