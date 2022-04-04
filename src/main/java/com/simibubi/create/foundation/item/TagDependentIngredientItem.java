package com.simibubi.create.foundation.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagDependentIngredientItem extends Item {

	private TagKey<Item> tag;

	public TagDependentIngredientItem(Properties properties, TagKey<Item> tag) {
		super(properties);
		this.tag = tag;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
		if (!shouldHide())
			super.fillItemCategory(tab, list);
	}

	public boolean shouldHide() {
		boolean tagMissing = !Registry.ITEM.isKnownTagName(this.tag);
		boolean tagEmpty = tagMissing || !Registry.ITEM.getTagOrEmpty(this.tag).iterator().hasNext();
		return tagMissing || tagEmpty;
	}

}
