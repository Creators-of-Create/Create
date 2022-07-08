package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;

import net.minecraft.network.chat.TextComponent;

public abstract class NumericSingleLineDisplaySource extends SingleLineDisplaySource {

	protected static final TextComponent ZERO = new TextComponent("0");
	
	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return "Number";
	}
	
	@Override
	protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
	}

}
