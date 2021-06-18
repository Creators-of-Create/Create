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
	}
	
	public static void interaction(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("fluid_pipe_interaction", "Draining and Filling fluid containers");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
	public static void encasing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("encased_fluid_pipe", "Encasing Fluid Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
	public static void valve(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("valve_pipe", "Controlling Fluid flow using Valves");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
	public static void smart(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("smart_pipe", "Controlling Fluid flow using Smart Pipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}
	
}
