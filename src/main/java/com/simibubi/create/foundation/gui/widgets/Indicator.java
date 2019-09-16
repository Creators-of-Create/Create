package com.simibubi.create.foundation.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.ScreenResources;

public class Indicator extends AbstractSimiWidget {
	
	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}
	
	public State state;
	
	public Indicator(int x, int y, String tooltip) {
		super(x, y, ScreenResources.INDICATOR.width, ScreenResources.INDICATOR.height);
		this.toolTip = ImmutableList.of(tooltip);
		this.state = State.OFF;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks ) {
		ScreenResources toDraw;
		switch(state) {
			case ON: toDraw = ScreenResources.INDICATOR_WHITE; break;
			case OFF: toDraw = ScreenResources.INDICATOR; break;
			case RED: toDraw = ScreenResources.INDICATOR_RED; break;
			case YELLOW: toDraw = ScreenResources.INDICATOR_YELLOW; break;
			case GREEN: toDraw = ScreenResources.INDICATOR_GREEN; break;
			default: toDraw = ScreenResources.INDICATOR; break;
		}
		toDraw.draw(this, x, y);
	}
	
}
