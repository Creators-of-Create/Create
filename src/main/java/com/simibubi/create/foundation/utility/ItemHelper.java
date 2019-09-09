package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class ItemHelper {

	public static List<ItemStack> multipliedOutput(ItemStack in, ItemStack out) {
		List<ItemStack> stacks = new ArrayList<>();
		ItemStack result = out.copy();
		result.setCount(in.getCount() * out.getCount());
		
		while (result.getCount() > result.getMaxStackSize()) {
			stacks.add(result.split(result.getMaxStackSize()));
		}
		
		stacks.add(result);
		return stacks;
	}

}
