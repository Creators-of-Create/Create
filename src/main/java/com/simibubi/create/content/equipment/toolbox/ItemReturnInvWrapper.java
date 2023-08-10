package com.simibubi.create.content.equipment.toolbox;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

/**
 * For inserting items into a players' inventory anywhere except the hotbar
 */
public class ItemReturnInvWrapper extends PlayerMainInvWrapper {

	public ItemReturnInvWrapper(Inventory inv) {
		super(inv);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot >= 0 && slot < 9)
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
