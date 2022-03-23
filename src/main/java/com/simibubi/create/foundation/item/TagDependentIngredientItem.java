package com.simibubi.create.foundation.item;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

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
		ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
		return !tagManager.isKnownTagName(tag) || tagManager.getTag(tag).isEmpty();
	}

}
