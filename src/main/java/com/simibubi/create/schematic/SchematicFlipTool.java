package com.simibubi.create.schematic;

public class SchematicFlipTool extends SchematicPlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = true;
	}

	@Override
	public boolean handleRightClick() {
		mirror();
		return true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		mirror();
		return true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();

		if (!schematicSelected)
			return;

		renderSelectedFace = selectedFace.getAxis().isHorizontal();
	}

	private void mirror() {
		if (schematicSelected && selectedFace.getAxis().isHorizontal()) {
			blueprint.flip(selectedFace.getAxis());
		}
	}

}
