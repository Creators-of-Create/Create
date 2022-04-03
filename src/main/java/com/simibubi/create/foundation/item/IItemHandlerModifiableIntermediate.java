package com.simibubi.create.foundation.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

interface IItemHandlerModifiableIntermediate extends Storage<ItemVariant> {

//	@Override
//	public default ItemStack getStackInSlot(int slot) {
//		return getStackInSlotIntermediate(slot);
//	}

	public ItemStack getStackInSlotIntermediate(int slot);

}
