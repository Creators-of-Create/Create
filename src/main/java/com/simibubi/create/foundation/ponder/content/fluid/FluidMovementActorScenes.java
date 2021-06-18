package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class FluidMovementActorScenes {

	public static void transfer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("portable_fluid_interface", "Contraption Fluid Exchange");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		/*
		 * Fluid Tanks on moving contraptions cannot be accessed by any pipes
		 * 
		 * This component can interact with fluid tanks without the need to stop the
		 * contraption
		 *
		 * Place a second one with a gap of 1 or 2 blocks inbetween
		 *
		 * Whenever they pass by each other, they will engage in a connection
		 *
		 * While engaged, the stationary interface will represent ALL Fluid Tanks on the
		 * contraption
		 *
		 * Fluid can now be inserted...
		 *
		 * ...or extracted from the contraption
		 *
		 * After no contents have been exchanged for a while, the contraption will
		 * continue on its way
		 */

	}

	public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("portable_fluid_interface_redstone", "Redstone Control");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Redstone power will prevent the stationary interface from engaging
		 */
		
	}

}
