package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class FluidTankScenes {
	
	public static void storage(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_tank_storage", "Storing Fluids in Fluid Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Fluid Tanks can be used to store large amounts of fluid
		 * 
		 * Pipe networks can push and pull fluids from any side
		 * 
		 * Comparators can read the current fill level of a fluid tank
		 * 
		 * In Survival Mode, Fluids cannot be added or taken manually
		 * 
		 * You can use Basins, Item Drains and Spouts to drain or fill fluid containing items
		 */
		
	}
	
	public static void sizes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_tank_sizes", "Dimensions of a Fluid tank");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Fluid Tanks can be combined to increase the total capacity
		 * 
		 * The base shape can be a square up to 3 blocks wide...
		 * 
		 * ...and grow in height by more than 30 additional layers
		 * 
		 * Using a Wrench, the tanks' window can be toggled
		 */
		
	}
	
	public static void creative(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("creative_fluid_tank", "Creative Fluid Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Creative Fluid Tanks can be used to provide a bottomless supply of fluid
		 * 
		 * Right-Click with a fluid containing item to configure it
		 * 
		 * Pipe Networks can now endlessly draw the assigned fluid from this tank
		 * 
		 * Any Fluids pipes push into a Creative Fluid Tank will be voided
		 */
		
	}
	
}
