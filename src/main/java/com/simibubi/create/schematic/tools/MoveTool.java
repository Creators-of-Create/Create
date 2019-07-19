package com.simibubi.create.schematic.tools;

public class MoveTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (!schematicSelected)
			return;

		renderSelectedFace = selectedFace.getAxis().isHorizontal();
	}
	
	@Override
	public boolean handleMouseWheel(double delta) {
		if (schematicSelected && selectedFace.getAxis().isHorizontal()) {
			blueprint.moveTo(delta < 0 ? blueprint.anchor.add(selectedFace.getDirectionVec())
					: blueprint.anchor.subtract(selectedFace.getDirectionVec()));
		}
		return true;
	}

}
