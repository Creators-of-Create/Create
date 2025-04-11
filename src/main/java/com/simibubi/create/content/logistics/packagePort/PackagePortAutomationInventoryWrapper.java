package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class PackagePortAutomationInventoryWrapper extends ItemHandlerWrapper {

	private PackagePortBlockEntity ppbe;

	private boolean access;

	public PackagePortAutomationInventoryWrapper(IItemHandlerModifiable wrapped, PackagePortBlockEntity ppbe) {
		super(wrapped);
		this.ppbe = ppbe;
		access = false;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (access)
			return super.extractItem(slot, amount, simulate);

		access = true;
		ItemStack extract = ItemHelper.extract(this, stack -> {
			if (!PackageItem.isPackage(stack))
				return false;
			String filterString = ppbe.getFilterString();
			boolean usesRegex = ppbe.usingRegex();
			return filterString != null && PackageItem.matchAddress(stack, filterString, usesRegex);
		}, simulate);
		access = false;

		return extract;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!PackageItem.isPackage(stack))
			return stack;
		String filterString = ppbe.getFilterString();
		boolean usesRegex = ppbe.usingRegex();
		if (filterString != null && PackageItem.matchAddress(stack, filterString, usesRegex))
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
