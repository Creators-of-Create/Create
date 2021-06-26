package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class SpoutScenes {

	public static void filling(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("spout_filling", "Filling Items using a Spout");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		/*
		 * The Spout can fill fluid holding items provided beneath it
		 * 
		 * The Input items can be placed on a Depot under the Spout
		 * 
		 * When items are provided on a belt...
		 * 
		 * The Spout will hold and process them automatically
		 */

	}

	public static void access(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("spout_access", "Moving fluids into Spouts");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * The internal fluid tank of a spout cannot be accessed manually
		 * 
		 * Instead, use an Item Drain or Basin to manually add fluid to your machines
		 */
		
	}

}
