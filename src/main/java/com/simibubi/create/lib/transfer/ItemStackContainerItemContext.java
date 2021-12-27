package com.simibubi.create.lib.transfer;

import com.simibubi.create.lib.extensions.ItemStackExtensions;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemStackContainerItemContext implements ContainerItemContext {
	private final SingleVariantStorage<ItemVariant> backingSlot = new SingleVariantStorage<>() {
		@Override
		protected ItemVariant getBlankVariant() {
			return ItemVariant.blank();
		}

		@Override
		public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
			if (maxAmount > Integer.MAX_VALUE) maxAmount = Integer.MAX_VALUE;
			long inserted = super.insert(itemVariant, maxAmount, transaction);
//			((ItemStackExtensions) (Object) backingStack).setItem(variant.getItem());
//			backingStack.setCount((int) inserted);
			return inserted;
		}

		@Override
		protected long getCapacity(ItemVariant variant) {
			return Integer.MAX_VALUE;
		}
	};

	private final ItemStack backingStack;

	public ItemStackContainerItemContext(ItemStack backingStack) {
		backingSlot.variant = ItemVariant.of(backingStack);
		backingSlot.amount = backingStack.getCount();
		this.backingStack = backingStack;
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return backingSlot;
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);
		// Always allow anything to be inserted.
		return maxAmount;
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		return Collections.emptyList();
	}
}
