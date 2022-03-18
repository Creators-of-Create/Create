package com.simibubi.create.foundation.item;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagDependentIngredientItem extends Item {

	private ResourceLocation tag;

	public TagDependentIngredientItem(Properties properties, ResourceLocation tag) {
		super(properties);
		this.tag = tag;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
		if (!shouldHide())
			super.fillItemCategory(tab, list);
	}

	public boolean shouldHide() {
		Tag<?> tag = ItemTags.getAllTags()
			.getTag(this.tag);
		return tag == null || tag.getValues()
			.isEmpty();
	}

}
