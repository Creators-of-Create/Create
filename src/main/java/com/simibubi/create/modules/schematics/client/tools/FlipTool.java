package com.simibubi.create.modules.schematics.client.tools;

public class FlipTool extends PlacementToolBase {

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
			schematicHandler.flip(selectedFace.getAxis());
		}
	}

}
