package com.simibubi.create.foundation.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.text.TextFormatting;

public class SelectionScrollInput extends ScrollInput {

	private final String scrollToSelect = Lang.translate("gui.scrollInput.scrollToSelect");
	protected List<String> options;

	public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
	}

	public ScrollInput forOptions(List<String> options) {
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
		toolTip.clear();
		toolTip.add(TextFormatting.BLUE + title);
		int min = Math.min(this.max - 16, state - 7);
		int max = Math.max(this.min + 16, state + 8);
		min = Math.max(min, this.min);
		max = Math.min(max, this.max);
		if (this.min + 1 == min)
			min--;
		if (min > this.min)
			toolTip.add(TextFormatting.GRAY + "> ...");
		if (this.max - 1 == max)
			max++;
		for (int i = min; i < max; i++) {
			StringBuilder result = new StringBuilder();
			if (i == state)
				result.append(TextFormatting.WHITE).append("-> ").append(options.get(i));
			else
				result.append(TextFormatting.GRAY).append("> ").append(options.get(i));
			toolTip.add(result.toString());
		}
		if (max < this.max)
			toolTip.add(TextFormatting.GRAY + "> ...");
		toolTip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + scrollToSelect);
	}

}
