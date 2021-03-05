package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class MovementActorScenes {
	
	public static void psiTransfer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Contraption Storage Exchange");
		scene.configureBasePlate(0, 0, 6);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
	}
	
	public static void psiRedstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Redstone Control");
		scene.configureBasePlate(0, 0, 6);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

}
