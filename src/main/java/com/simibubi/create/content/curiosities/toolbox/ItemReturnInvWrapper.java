package com.simibubi.create.content.curiosities.toolbox;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * For inserting items into a players' inventory anywhere except the hotbar
 */
public class ItemReturnInvWrapper extends InvWrapper {

	public ItemReturnInvWrapper(IInventory inv) {
		super(inv);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot >= 0 && slot < 9)
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
