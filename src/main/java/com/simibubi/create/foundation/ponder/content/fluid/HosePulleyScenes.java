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
		
		/*
		 * Hose Pulleys can be used to fill or drain large bodies of Fluid
		 * 
		 * With the Kinetic Input, the height of the pulleys' hose can be controlled
		 * 
		 * The Pulley will retract when the input rotation is inverted
		 * 
		 * Once the hose is in position, an attached pipe network can either provide fluid to the Hose Pulley...
		 * 
		 * ...or pull from it, draining the pool instead
		 * 
		 * Fill and Drain speed of the pulley depend entirely on the fluid networks' throughput
		 */
		
	}
	
	public static void level(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("hose_pulley_level", "Fill and Drain level of Hose Pulleys");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * While fully retracted, the Hose Pulley cannot operate
		 * 
		 * Draining runs from top to bottom
		 * 
		 * The surface level will end up just below where the hose ends
		 * 
		 * Filling runs from bottom to top
		 * 
		 * The filled pool will not grow beyond the layer above the hose end
		 */
		
	}
	
	public static void infinite(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("hose_pulley_infinite", "Passively Filling and Draining large bodies of Fluid");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * When deploying the Hose Pulley into a large enough ocean...
		 * 
		 * It will simply provide/dispose fluids without affecting the source
		 * 
		 * Pipe networks can limitlessly pull or push fluids to and from such pulleys
		 * 
		 * After such a state is reached, your goggles will display an indicator when looking at it
		 */
		
	}
	
}
