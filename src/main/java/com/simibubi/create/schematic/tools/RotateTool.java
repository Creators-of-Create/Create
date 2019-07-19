package com.simibubi.create.schematic.tools;

import net.minecraft.util.Rotation;

public class RotateTool extends PlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		blueprint.rotate(delta > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
		return true;
	}

}
