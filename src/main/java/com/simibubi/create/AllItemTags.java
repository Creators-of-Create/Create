package com.simibubi.create;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public enum AllItemTags {

	;

	public Tag<Item> tag;

	private AllItemTags(String path) {
		tag = new ItemTags.Wrapper(new ResourceLocation(Create.ID, path + "/" + Lang.asId(name())));
	}

	public boolean matches(ItemStack item) {
		return tag.contains(item.getItem());
	}

}
