package com.simibubi.create.lib.transfer.fluid;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class SingleItemStackContext implements ContainerItemContext {
	protected ItemStack stack;
	protected final Level level;
	protected final OneSlotStorage stackStorage;

	public SingleItemStackContext(ItemStack stack, Level level) {
		this.stack = stack;
		this.level = level;
		stackStorage = new OneSlotStorage(stack, this);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return stackStorage;
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		return stackStorage.insert(itemVariant, maxAmount, transactionContext);
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		return ObjectLists.EMPTY_LIST;
	}

	public static class OneSlotStorage extends SingleStackStorage {
		protected ItemStack stack;
		protected final SingleItemStackContext owner;

		public OneSlotStorage(ItemStack stack, SingleItemStackContext owner) {
			this.stack = stack;
			this.owner = owner;
		}

		@Override
		protected ItemStack getStack() {
			return stack;
		}

		@Override
		protected void setStack(ItemStack stack) {
			this.stack = stack;
			owner.stack = stack;
		}
	}
}
