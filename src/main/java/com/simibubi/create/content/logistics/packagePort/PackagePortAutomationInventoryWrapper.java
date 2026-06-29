package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class PackagePortAutomationInventoryWrapper extends ItemHandlerWrapper {
	private final PackagePortBlockEntity ppbe;

	public PackagePortAutomationInventoryWrapper(IItemHandlerModifiable wrapped, PackagePortBlockEntity ppbe) {
		super(wrapped);
		this.ppbe = ppbe;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack preview = super.extractItem(slot, 64, true);

		if (!PackageItem.isPackage(preview))
			return ItemStack.EMPTY;

		String filterString = ppbe.getFilterString();
		if (filterString == null || !PackageItem.matchAddress(preview, filterString))
			return ItemStack.EMPTY;

		return simulate ? preview : super.extractItem(slot, amount, false);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!PackageItem.isPackage(stack))
			return stack;
		String filterString = ppbe.getFilterString();
		if (filterString != null && PackageItem.matchAddress(stack, filterString))
			return stack;
		return super.insertItem(slot, stack, simulate);
	}
}
