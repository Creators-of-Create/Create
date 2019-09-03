package com.simibubi.create.modules.schematics.client.tools;

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
			schematicHandler.moveTo(delta < 0 ? schematicHandler.anchor.add(selectedFace.getDirectionVec())
					: schematicHandler.anchor.subtract(selectedFace.getDirectionVec()));
		}
		return true;
	}

}
