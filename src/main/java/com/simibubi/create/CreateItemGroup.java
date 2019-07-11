package com.simibubi.create;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class CreateItemGroup extends ItemGroup {

	public CreateItemGroup() {
		super(getGroupCountSafe(), Create.ID);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(AllItems.SYMMETRY_WAND.get());
	}
}
