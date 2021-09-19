package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.Arrays;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

import static com.simibubi.create.AllShapes.shape;

public class GaugeShaper extends VoxelShaper {

	private VoxelShaper axisFalse, axisTrue;

	static GaugeShaper make(){
		GaugeShaper shaper = new GaugeShaper();
		VoxelShape GAUGE_SHAPE_UP = shape(1, 0, 0, 15, 2, 16).add(2, 2, 1, 14, 14, 15).build();
		shaper.axisFalse = forDirectional(GAUGE_SHAPE_UP, Direction.UP);
		shaper.axisTrue = forDirectional(rotatedCopy(AllShapes.GAUGE_SHAPE_UP, new Vec3(0, 90, 0)), Direction.UP);
		//shapes for X axis need to be swapped
		Arrays.asList(Direction.EAST, Direction.WEST).forEach(direction -> {
			VoxelShape mem = shaper.axisFalse.get(direction);
			shaper.axisFalse.withShape(shaper.axisTrue.get(direction), direction);
			shaper.axisTrue.withShape(mem, direction);
		});
		return shaper;
	}

	public VoxelShape get(Direction direction, boolean axisAlong) {
		return (axisAlong ? axisTrue : axisFalse).get(direction);
	}
}
