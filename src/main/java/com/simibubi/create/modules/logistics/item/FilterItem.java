package com.simibubi.create.modules.logistics.item;

import com.simibubi.create.foundation.item.InfoItem;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class FilterItem extends InfoItem {

	public static class Color implements IItemColor {
		@Override
		public int getColor(ItemStack stack, int layer) {
			if (layer == 0)
				return 0xFFFFFF;
			if (layer == 1)
				return 0x6677AA;
			if (layer == 2)
				return 0x334477;
			return 0;
		}
	}

	public FilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Yellow;
		return new ItemDescription(color)
				.withSummary("Holds information for controlling input, output and detection of objects.")
				.withControl("R-Click while Sneaking", "Opens the " + h("Configuration Screen", color))
				.withControl("When Entity punched", "Creates a filter with the target set to the Entity.").createTabs();
	}

}
