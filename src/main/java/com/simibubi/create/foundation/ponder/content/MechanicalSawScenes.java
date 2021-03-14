package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class MechanicalSawScenes {

	public static void processing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_saw_processing", "Processing Items on the Mechanical Saw");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		
		BlockPos shaftPos = util.grid.at(2, 1, 3);
		
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

	public static void treeCutting(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_saw_breaker", "Cutting Trees with the Mechanical Saw");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

	public static void contraption(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_saw_contraption", "Using Mechanical Saws on Contraptions");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

}
