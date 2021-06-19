package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class PumpScenes {
	
	public static void flow(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_pump_flow", "Fluid Transportation using Mechanical Pumps");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Mechanical Pumps govern the flow of their attached pipe networks
		 * 
		 * When powered, their arrow indicates the direction of flow
		 * 
		 * The network behind is now pulling fluids...
		 * 
		 * ...while the network in front is transferring it outward
		 * 
		 * Reversing the input rotation reverses the direction of flow
		 * 
		 * Use a Wrench to reverse the orientation of pumps manually
		 */
	}
	
	public static void speed(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_pump_speed", "Throughput of Mechanical Pumps");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Regardless of speed, Mechanical Pumps affect pipes up to 16 blocks away
		 * 
		 * Speeding up the input rotation changes the speed of flows building...
		 * 
		 * ...aswell as how quickly fluids are transferred
		 * 
		 * Parallel pumps combine their speed within shared pipe connections
		 * 
		 * Alternating their orientation can help lining up their flow directions
		 */
		
	}
	
}
