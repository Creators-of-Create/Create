package com.simibubi.create.content.curiosities.toolbox;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import com.simibubi.create.lib.transfer.item.InvWrapper;

/**
 * For inserting items into a players' inventory anywhere except the hotbar
 */
public class ItemReturnInvWrapper extends InvWrapper {

	public ItemReturnInvWrapper(Container inv) {
		super(inv);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot >= 0 && slot < 9)
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
