package com.simibubi.create.foundation.item;

import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;

interface IItemHandlerModifiableIntermediate extends IItemHandlerModifiable {

	@Override
	public default ItemStack getStackInSlot(int slot) {
		return getStackInSlotIntermediate(slot);
	}

	public ItemStack getStackInSlotIntermediate(int slot);

}
