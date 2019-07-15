package com.simibubi.create.schematic;

import net.minecraft.util.Rotation;

public class SchematicRotateTool extends SchematicPlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		blueprint.rotate(delta > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
		return true;
	}

}
