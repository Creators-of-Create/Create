package com.simibubi.create.foundation.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class TagDependentIngredientItem extends Item {

	private TagKey<Item> tag;

	public TagDependentIngredientItem(Properties properties, TagKey<Item> tag) {
		super(properties);
		this.tag = tag;
	}

	public boolean shouldHide() {
		ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
		return !tagManager.isKnownTagName(tag) || tagManager.getTag(tag).isEmpty();
	}

}
