package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

@Mixin(ItemTags.class)
public interface ItemTagsAccessor {
	@Invoker("bind")
	static Tag.Named<Item> callBind(String string) {
		throw new UnsupportedOperationException();
	}
}
