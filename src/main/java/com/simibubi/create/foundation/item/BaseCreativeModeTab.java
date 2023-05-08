package com.simibubi.create.foundation.item;

import com.simibubi.create.AllBlocks;

import net.minecraft.world.item.ItemStack;

public class BaseCreativeModeTab extends CreateCreativeModeTab {
	public BaseCreativeModeTab() {
		super("base");
	}

	@Override
	public ItemStack makeIcon() {
		return AllBlocks.COGWHEEL.asStack();
	}
}
