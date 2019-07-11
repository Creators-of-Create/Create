package com.simibubi.create.gui.widgets;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.widget.Widget;

public abstract class AbstractSimiWidget extends Widget {

	protected List<String> toolTip;
	
	public AbstractSimiWidget(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn, "");
		toolTip = new LinkedList<>();
	}
	
	public List<String> getToolTip() {
		return toolTip;
	}
	
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
	}

}
