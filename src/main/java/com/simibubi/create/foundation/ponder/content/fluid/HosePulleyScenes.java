package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class HosePulleyScenes {
	
	public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("hose_pulley", "Source Filling and Draining using Hose Pulleys");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
	public static void level(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("hose_pulley_level", "Fill and Drain level of Hose Pulleys");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
	public static void infinite(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("hose_pulley_infinite", "Passively Filling and Draining large bodies of Fluid");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
}
