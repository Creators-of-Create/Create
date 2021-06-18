package com.simibubi.create.foundation.ponder.content.fluid;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

import net.minecraft.util.Direction;

public class DrainScenes {
	
	public static void emptying(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("item_drain", "Emptying Fluid Containers using Item Drains");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		/*
		 * Item Drains can extract fluids from any fluid container items
		 * 
		 * Right-click it to pour fluids from your held item into it
		 * 
		 * When items are inserted from the side...
		 * 
		 * ...they tumble across the surface, emptying out their contained fluid if possible
		 * 
		 * Pipe Networks can now pull the fluid from the drains' internal buffer
		 */
		
	}
	
}
