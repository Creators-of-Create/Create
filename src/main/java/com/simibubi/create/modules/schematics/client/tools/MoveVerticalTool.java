package com.simibubi.create.modules.schematics.client.tools;

public class MoveVerticalTool extends PlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		if (blueprint.deployed) {
			blueprint.moveTo(blueprint.anchor.add(0, delta, 0));
		}
		return true;
	}
	
}
