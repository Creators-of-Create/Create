package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;

import net.minecraft.network.chat.TextComponent;

public abstract class NumericSingleLineDataSource extends SingleLineDataSource {

	protected static final TextComponent ZERO = new TextComponent("0");
	
	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return "Number";
	}
	
	@Override
	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "numeric", false, false);
	}

}
