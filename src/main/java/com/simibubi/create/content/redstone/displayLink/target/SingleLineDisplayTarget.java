package com.simibubi.create.content.redstone.displayLink.target;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class SingleLineDisplayTarget extends DisplayTarget {

	@Override
	public final void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
		acceptLine(
			text.size() == 1 ?
				text.get(0) :
				Component.literal(text.stream().map(Component::getString).filter(section -> !section.isBlank()).collect(Collectors.joining(" ")))
			, context);
	}

	protected abstract void acceptLine(MutableComponent text, DisplayLinkContext context);

	@Override
	public final DisplayTargetStats provideStats(DisplayLinkContext context) {
		return new DisplayTargetStats(1, getWidth(context), this);
	}

	@Override
	public Component getLineOptionText(int line) {
		return CreateLang.translateDirect("display_target.single_line");
	}

	protected abstract int getWidth(DisplayLinkContext context);

}
