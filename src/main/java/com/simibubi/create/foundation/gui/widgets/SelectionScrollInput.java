package com.simibubi.create.foundation.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SelectionScrollInput extends ScrollInput {

	private final IFormattableTextComponent scrollToSelect = Lang.translate("gui.scrollInput.scrollToSelect");
	protected List<ITextComponent> options;

	public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
	}

	public ScrollInput forOptions(List<ITextComponent> options) {
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
		toolTip.add(title.copy().formatted(TextFormatting.BLUE));
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(StringTextComponent.EMPTY.copy().append("-> ").append(options.get(i)).formatted(TextFormatting.WHITE));
			else
				toolTip.add(StringTextComponent.EMPTY.copy().append("> ").append(options.get(i)).formatted(TextFormatting.GRAY));
		}
		toolTip.add( StringTextComponent.EMPTY.copy().append(scrollToSelect).formatted(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
	}

}
