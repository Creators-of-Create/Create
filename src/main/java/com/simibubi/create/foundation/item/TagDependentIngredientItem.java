package com.simibubi.create.foundation.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.Item.Properties;

public class TagDependentIngredientItem extends Item {

	private ResourceLocation tag;

	public TagDependentIngredientItem(Properties p_i48487_1_, ResourceLocation tag) {
		super(p_i48487_1_);
		this.tag = tag;
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (!shouldHide())
			super.fillItemCategory(p_150895_1_, p_150895_2_);
	}

	public boolean shouldHide() {
		Tag<?> tag = ItemTags.getAllTags()
			.getTag(this.tag);
		return tag == null || tag.getValues()
			.isEmpty();
	}

}
