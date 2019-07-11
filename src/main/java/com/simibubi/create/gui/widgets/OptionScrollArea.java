package com.simibubi.create.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.TextFormatting;

public class OptionScrollArea extends ScrollArea {

	protected List<String> options;

	public OptionScrollArea(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
	}

	public ScrollArea forOptions(List<String> options) {
		this.options = options;
		this.max = options.size();
		updateTooltip();
		return this;
	}

	@Override
	protected void writeToLabel() {
		displayLabel.text = options.get(state);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return super.mouseScrolled(mouseX, mouseY, -delta);
	}

	@Override
	protected void updateTooltip() {
		super.updateTooltip();
		for (int i = min; i < max; i++) {
			StringBuilder result = new StringBuilder();
			if (i == state)
				result.append(TextFormatting.WHITE).append("-> ").append(options.get(i));
			else
				result.append(TextFormatting.GRAY).append("> ").append(options.get(i));
			toolTip.add(result.toString());
		}
	}

}
