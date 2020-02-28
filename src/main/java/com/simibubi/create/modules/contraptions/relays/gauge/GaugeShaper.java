package com.simibubi.create.modules.contraptions.relays.gauge;

import java.util.Arrays;

import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

public class GaugeShaper extends VoxelShaper {

	private VoxelShaper axisFalse, axisTrue;

	static GaugeShaper make(){
		GaugeShaper shaper = new GaugeShaper();
		shaper.axisFalse = forDirectional(AllShapes.GAUGE_SHAPE_UP, Direction.UP);
		shaper.axisTrue = forDirectional(rotatedCopy(AllShapes.GAUGE_SHAPE_UP, new Vec3d(0, 90, 0)), Direction.UP);
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