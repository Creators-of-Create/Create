package com.simibubi.create.foundation.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class TagDependentIngredientItem extends Item {

	private ResourceLocation tag;

	public TagDependentIngredientItem(Properties p_i48487_1_, ResourceLocation tag) {
		super(p_i48487_1_);
		this.tag = tag;
	}

	@Override
	public void fillItemGroup(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (!shouldHide())
			super.fillItemGroup(p_150895_1_, p_150895_2_);
	}

	public boolean shouldHide() {
		ITag<?> tag = ItemTags.getCollection()
			.get(this.tag);
		return tag == null || tag.values()
			.isEmpty();
	}

}
