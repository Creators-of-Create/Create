package com.simibubi.create.modules.schematics.client.tools;

import net.minecraft.util.Rotation;

public class RotateTool extends PlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		schematicHandler.rotate(delta > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
		return true;
	}

}
