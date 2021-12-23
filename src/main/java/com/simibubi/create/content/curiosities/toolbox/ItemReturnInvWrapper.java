package com.simibubi.create.content.curiosities.toolbox;

import com.simibubi.create.lib.transfer.item.InvWrapper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * For inserting items into a players' inventory anywhere except the hotbar
 */
public class ItemReturnInvWrapper extends InvWrapper {

	public ItemReturnInvWrapper(Inventory inv) {
		super(inv);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot >= 0 && slot < 9)
			return stack;
		if (slot > 36)
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
