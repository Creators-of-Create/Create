package com.simibubi.create.foundation.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SelectionScrollInput extends ScrollInput {

	private final MutableComponent scrollToSelect = Lang.translateDirect("gui.scrollInput.scrollToSelect");
	protected List<? extends Component> options;

	public SelectionScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		options = new ArrayList<>();
		inverted();
	}

	public ScrollInput forOptions(List<? extends Component> options) {
		this.options = options;
		this.max = options.size();
		format(options::get);
		updateTooltip();
		return this;
	}

	@Override
	protected void updateTooltip() {
		toolTip.clear();
		if (title == null)
			return;
		toolTip.add(title.plainCopy()
			.withStyle(s -> s.withColor(HEADER_RGB)));
		int min = Math.min(this.max - 16, state - 7);
		int max = Math.max(this.min + 16, state + 8);
		min = Math.max(min, this.min);
		max = Math.min(max, this.max);
		if (this.min + 1 == min)
			min--;
		if (min > this.min)
			toolTip.add(Components.literal("> ...")
				.withStyle(ChatFormatting.GRAY));
		if (this.max - 1 == max)
			max++;
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(Components.empty()
					.append("-> ")
					.append(options.get(i))
					.withStyle(ChatFormatting.WHITE));
			else
				toolTip.add(Components.empty()
					.append("> ")
					.append(options.get(i))
					.withStyle(ChatFormatting.GRAY));
		}
		if (max < this.max)
			toolTip.add(Components.literal("> ...")
				.withStyle(ChatFormatting.GRAY));

		if (hint != null)
			toolTip.add(hint.plainCopy()
				.withStyle(s -> s.withColor(HINT_RGB)));
		toolTip.add(scrollToSelect.plainCopy()
			.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

}
