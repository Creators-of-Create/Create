package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class PipeScenes {

	public static void flow(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_pipe_flow", "Moving Fluids using Copper Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

//		scene.overlay.showText(50)
//			.text("")
//			.attachKeyFrame()
//			.placeNearTarget()
//			.pointAt(util.vector.topOf(2, 0, 2));

		/*
		 * Use Fluid Pipes to connect two fluid sources, containers or empty spaces
		 * 
		 * Using a wrench, a straight pipe segment can be given a window
		 * 
		 * Windowed pipes will not connect to any other adjancent pipe segements
		 * 
		 * Powered by Mechanical Pumps, Fluid Pipes can be used to transport Fluids between endpoints
		 * 
		 * Until the flow finds a target, nothing is extracted from the source
		 * 
		 * Once a connection is established, fluids are gradually transferred between the endpoints
		 * 
		 * Thus, the Pipe blocks themselves never 'physically' contain any fluid
		 */
	}

	public static void interaction(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_pipe_interaction", "Draining and Filling fluid containers");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Endpoints of a pipe network can interact with a variety of blocks
		 * 
		 * Any block with fluid storage capabities can be filled or drained
		 * 
		 * Source blocks right in front of an open end can be picked up...
		 * 
		 * ...while spilling into empty spaces can create fluid sources
		 * 
		 * Pipes can also extract fluids from a handful of blocks directly
		 */
	}

	public static void encasing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("encased_fluid_pipe", "Encasing Fluid Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Copper Casing can be used to decorate Fluid Pipes
		 * 
		 * Aside from being conceiled, encased pipes are locked into their connectivity state
		 * 
		 * Adding and Removing Pipes around it will no longer affect its shape
		 */
	}

	public static void valve(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("valve_pipe", "Controlling Fluid flow using Valves");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Valve pipes propagate flows in a straight line
		 * 
		 * When given Rotational Force in the closing direction, the valve will stop the fluid flow
		 * 
		 * It can be re-opened by reversing the input rotation
		 */
	}

	public static void smart(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("smart_pipe", "Controlling Fluid flow using Smart Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Smart pipes propagate flows in a straight line
		 * 
		 * When placed directly at the source, they can specify the type of fluid to extract
		 * 
		 * Simply Right-Click their filter slot with any item containing the desired fluid
		 * 
		 * When placed further down a pipe network, smart pipes will only let matching fluids continue past
		 * 
		 * In this configuration, their filter has no impact on whether a fluid can enter the pipe network 
		 */
	}

}
