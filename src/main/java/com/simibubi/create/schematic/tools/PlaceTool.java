package com.simibubi.create.schematic.tools;

public class PlaceTool extends SchematicToolBase {

	@Override
	public boolean handleRightClick() {
		blueprint.printInstantly();
		return true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}

}
