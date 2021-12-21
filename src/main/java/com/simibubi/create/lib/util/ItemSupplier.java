package com.simibubi.create.lib.util;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;

public class ItemSupplier implements Supplier<Item> {
	private Item item;

	@Override
	public Item get() {
		return item;
	}

	public ItemSupplier(Item item) {
		this.item = item;
	}
}
