package com.simibubi.create.foundation.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class LegacyItemHandlerAdapter implements IItemHandlerModifiable {
	private final ResourceHandler<ItemResource> handler;

	public LegacyItemHandlerAdapter(ResourceHandler<ItemResource> handler) {
		this.handler = handler;
	}

	public static IItemHandler of(ResourceHandler<ItemResource> handler) {
		return handler == null ? null : new LegacyItemHandlerAdapter(handler);
	}

	public static IItemHandlerModifiable modifiable(ResourceHandler<ItemResource> handler) {
		return handler == null ? null : new LegacyItemHandlerAdapter(handler);
	}

	@Override
	public int getSlots() {
		return handler.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemUtil.getStack(handler, slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return ItemUtil.insertItemReturnRemaining(handler, slot, stack, simulate, null);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0)
			return ItemStack.EMPTY;
		ItemResource resource = handler.getResource(slot);
		if (resource.isEmpty())
			return ItemStack.EMPTY;
		amount = Math.min(amount, resource.getMaxStackSize());
		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = handler.extract(slot, resource, amount, transaction);
			if (!simulate)
				transaction.commit();
			return resource.toStack(extracted);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		try (Transaction transaction = Transaction.openRoot()) {
			ItemResource current = handler.getResource(slot);
			int currentAmount = handler.getAmountAsInt(slot);
			if (!current.isEmpty() && currentAmount > 0)
				handler.extract(slot, current, currentAmount, transaction);

			if (!stack.isEmpty()) {
				int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
				if (inserted != stack.getCount())
					return;
			}

			transaction.commit();
		}
	}
}
