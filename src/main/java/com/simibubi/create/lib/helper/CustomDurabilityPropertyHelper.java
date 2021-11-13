package com.simibubi.create.lib.helper;

import net.minecraft.world.item.Item;

public final class CustomDurabilityPropertyHelper extends Item.Properties {
	public static Item.Properties setMaxDamage(int damage, Item.Properties properties) {
		Item.Properties newProperty = properties;
		newProperty.durability(damage);
		return newProperty;
	}
}
