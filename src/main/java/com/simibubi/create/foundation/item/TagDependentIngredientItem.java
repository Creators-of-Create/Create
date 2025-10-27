package com.simibubi.create.foundation.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagDependentIngredientItem extends Item {

	private TagKey<Item> tag;

	public TagDependentIngredientItem(Properties properties, TagKey<Item> tag) {
		super(properties);
		this.tag = tag;
	}

	public boolean shouldHide() {
		for (Holder<Item> ignored : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
			return false; // at least 1 present
		}
		return true; // none present
	}

}
