package com.simibubi.create.content.schematics.client.tools;

import net.minecraft.util.Mth;

public class MoveVerticalTool extends PlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		if (schematicHandler.isDeployed()) {
			schematicHandler.getTransformation().move(0, Mth.sign(delta), 0);
			schematicHandler.markDirty();
		}
		return true;
	}
	
}
