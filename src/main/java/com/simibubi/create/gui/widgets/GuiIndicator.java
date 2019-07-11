package com.simibubi.create.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.gui.GuiResources;

public class GuiIndicator extends AbstractSimiWidget {
	
	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}
	
	public State state;
	
	public GuiIndicator(int x, int y, String tooltip) {
		super(x, y, GuiResources.INDICATOR.width, GuiResources.INDICATOR.height);
		this.toolTip = ImmutableList.of(tooltip);
		this.state = State.OFF;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks ) {
		GuiResources toDraw;
		switch(state) {
			case ON: toDraw = GuiResources.INDICATOR_WHITE; break;
			case OFF: toDraw = GuiResources.INDICATOR; break;
			case RED: toDraw = GuiResources.INDICATOR_RED; break;
			case YELLOW: toDraw = GuiResources.INDICATOR_YELLOW; break;
			case GREEN: toDraw = GuiResources.INDICATOR_GREEN; break;
			default: toDraw = GuiResources.INDICATOR; break;
		}
		toDraw.draw(this, x, y);
	}
	
}
