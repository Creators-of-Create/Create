package com.simibubi.create.modules.logistics.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FilterItem extends Item {

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

}
