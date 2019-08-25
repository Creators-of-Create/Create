package com.simibubi.create.foundation.utility;

import java.util.List;

import net.minecraft.util.text.ITextComponent;

public class TooltipHolder {

	private ItemDescription toolTip;
	
	public TooltipHolder(ITooltip item) {
		toolTip = item.getDescription();
	}

	public void addInformation(List<ITextComponent> tooltip) {
		toolTip.addInformation(tooltip);
	}

}
