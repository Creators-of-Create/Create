package com.simibubi.create.lib.mixin.common.accessor;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.TagValue.class)
public interface TagValueAccessor {
	@Invoker("<init>")
	static Ingredient.TagValue createTagValue(Tag<Item> tag) {
		throw new UnsupportedOperationException();
	}
}
