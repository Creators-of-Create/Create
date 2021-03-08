package com.simibubi.create.foundation.ponder;

public class PonderElement {

	boolean visible = true;

	public void tick(PonderScene scene) {}
	
	public void reset(PonderScene scene) {}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}
