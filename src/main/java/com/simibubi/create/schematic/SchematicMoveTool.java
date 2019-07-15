package com.simibubi.create.schematic;

public class SchematicMoveTool extends SchematicPlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {

		if (schematicSelected && selectedFace.getAxis().isHorizontal()) {
			blueprint.moveTo(delta < 0 ? blueprint.anchor.add(selectedFace.getDirectionVec())
					: blueprint.anchor.subtract(selectedFace.getDirectionVec()));
			return true;
		}

		return super.handleMouseWheel(delta);
	}

}
