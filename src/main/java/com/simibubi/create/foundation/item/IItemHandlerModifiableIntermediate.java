package com.simibubi.create.foundation.item;

import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;

interface IItemHandlerModifiableIntermediate extends IItemHandlerModifiable {

	@Override
	public default ItemStack getStackInSlot(int slot) {
		return getStackInSlotIntermediate(slot);
	}

	public ItemStack getStackInSlotIntermediate(int slot);

}
