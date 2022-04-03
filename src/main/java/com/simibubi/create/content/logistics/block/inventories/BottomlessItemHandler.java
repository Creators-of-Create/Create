package com.simibubi.create.content.logistics.block.inventories;

import java.util.Iterator;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler implements SingleSlotStorage<ItemVariant> {

	private Supplier<ItemStack> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return maxAmount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = suppliedItemStack.get();
		if (stack == null || !resource.matches(stack))
			return 0;
		if (!stack.isEmpty())
			return Math.min(stack.getMaxStackSize(), maxAmount);
		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return suppliedItemStack.get() == null;
	}

	@Override
	public ItemVariant getResource() {
		ItemStack stack = suppliedItemStack.get();
		return stack == null ? ItemVariant.blank() : ItemVariant.of(stack);
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return SingleSlotStorage.super.iterator(transaction);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = suppliedItemStack.get();
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
		return stack;
	}

	@Override
	protected void readSnapshot(SnapshotData snapshot) {
	}

	@Override
	protected SnapshotData createSnapshot() {
		return BottomlessSnapshotData.INSTANCE;
	}

	public static class BottomlessSnapshotData extends SnapshotData {
		public static final BottomlessSnapshotData INSTANCE = new BottomlessSnapshotData(null);
		public BottomlessSnapshotData(ItemStack[] stacks) {
			super(stacks);
		}
	}
}
