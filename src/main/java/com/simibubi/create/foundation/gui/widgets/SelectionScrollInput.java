package com.simibubi.create.foundation.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class SelectionScrollInput extends ScrollInput {

	private final MutableComponent scrollToSelect = Lang.translate("gui.scrollInput.scrollToSelect");
	protected List<Component> options;

	public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
	}

	public ScrollInput forOptions(List<Component> options) {
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
		toolTip.add(title.plainCopy().withStyle(ChatFormatting.BLUE));
		int min = Math.min(this.max - 16, state - 7);
		int max = Math.max(this.min + 16, state + 8);
		min = Math.max(min, this.min);
		max = Math.min(max, this.max);
		if (this.min + 1 == min)
			min--;
		if (min > this.min)
			toolTip.add(new TextComponent("> ...").withStyle(ChatFormatting.GRAY));
		if (this.max - 1 == max)
			max++;
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(TextComponent.EMPTY.plainCopy().append("-> ").append(options.get(i)).withStyle(ChatFormatting.WHITE));
			else
				toolTip.add(TextComponent.EMPTY.plainCopy().append("> ").append(options.get(i)).withStyle(ChatFormatting.GRAY));
		}
		if (max < this.max)
			toolTip.add(new TextComponent("> ...").withStyle(ChatFormatting.GRAY));

		toolTip.add(scrollToSelect.plainCopy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

}
