package com.simibubi.create.foundation.ponder.element;

import com.simibubi.create.foundation.ponder.PonderScene;

public class PonderElement {

	boolean visible = true;

	public void whileSkipping(PonderScene scene) {}

	public void tick(PonderScene scene) {}

	public void reset(PonderScene scene) {}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
