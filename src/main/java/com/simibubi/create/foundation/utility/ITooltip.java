package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.utility.ItemDescription.Palette;

public interface ITooltip {

	public ItemDescription getDescription();

	public default String h(String s, Palette palette) {
		return ItemDescription.hightlight(s, palette);
	}
	
}
