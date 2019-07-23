package com.simibubi.create.modules.schematics.client.tools;

public abstract class PlacementToolBase extends SchematicToolBase {

	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void updateSelection() {
		super.updateSelection();
	}
	
	@Override
	public void renderTool() {
		super.renderTool();
	}

	@Override
	public void renderOverlay() {
		super.renderOverlay();
	}
	
	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}
	
	@Override
	public boolean handleRightClick() {
		return false;
	}
	
}
