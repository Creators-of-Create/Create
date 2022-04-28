package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class BasinInventory extends SmartInventory {

	private BasinTileEntity te;

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
		this.te = te;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!insertionAllowed)
			return 0;
		// Only insert if no other slot already has a stack of this item
		for (int i = 0; i < handler.stacks.length; i++) {
			ItemStack stack = handler.stacks[i];
			if (resource.matches(stack) && isItemValid(i, resource)) { // we already have this item - make sure it all fits in 1 stack
				int max = stack.getMaxStackSize();
				int space = max - stack.getCount();
				int toInsert = (int) Math.min(space, maxAmount);
				if (toInsert > 0) {
					ItemStack newStack = stack.copy();
					updateSnapshots(transaction);
					newStack.grow(toInsert);
					contentsChangedInternal(i, newStack, transaction);
					return toInsert;
				}
				// found match but didn't insert - give up, we only allow 1 stack.
				return 0;
			}
		}

		// haven't found an existing stack - add new
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		te.notifyChangeOfContents();
	}
}
