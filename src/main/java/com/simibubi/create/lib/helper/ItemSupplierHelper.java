package com.simibubi.create.lib.helper;

import java.util.function.Supplier;

import com.simibubi.create.lib.extensions.ItemExtensions;

import net.minecraft.world.item.Item;

public class ItemSupplierHelper {
	public static Supplier<Item> getSupplier(Item item) {
		return ((ItemExtensions) item).create$getSupplier();
	}
}
