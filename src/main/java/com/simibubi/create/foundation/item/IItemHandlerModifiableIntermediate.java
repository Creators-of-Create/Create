package com.simibubi.create.foundation.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

interface IItemHandlerModifiableIntermediate extends IItemHandlerModifiable {

	@Override
	public default ItemStack getStackInSlot(int slot) {
		return getStackInSlotIntermediate(slot);
	}

	public ItemStack getStackInSlotIntermediate(int slot);

}