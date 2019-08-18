package com.simibubi.create.foundation.item;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.ItemDescription;

public class TooltipCache {

	private ItemDescription toolTip;

	public ItemDescription getOrCreate(Supplier<ItemDescription> factory) {
		if (toolTip == null)
			toolTip = factory.get();
		return toolTip;
	}

}
