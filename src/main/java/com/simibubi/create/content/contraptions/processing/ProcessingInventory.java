package com.simibubi.create.content.contraptions.processing;

import java.util.function.Consumer;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ProcessingInventory extends ItemStackHandler {
	public float remainingTime;
	public float recipeDuration;
	public boolean appliedRecipe;
	public Consumer<ItemStack> callback;
	private boolean limit;

	public ProcessingInventory(Consumer<ItemStack> callback) {
		super(16);
		this.callback = callback;
	}

	public ProcessingInventory withSlotLimit(boolean limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public int getSlotLimit(int slot) {
		return !limit ? super.getSlotLimit(slot) : 1;
	}

	public void clear() {
		TransferUtil.clearStorage(this);
		remainingTime = 0;
		recipeDuration = 0;
		appliedRecipe = false;
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSlots(); i++)
			if (!getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long inserted = super.insert(resource, maxAmount, transaction);
		if (inserted != 0)
			TransactionCallback.onSuccess(transaction, () -> callback.accept(resource.toStack((int) inserted)));
		return inserted;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putFloat("ProcessingTime", remainingTime);
		nbt.putFloat("RecipeTime", recipeDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		remainingTime = nbt.getFloat("ProcessingTime");
		recipeDuration = nbt.getFloat("RecipeTime");
		appliedRecipe = nbt.getBoolean("AppliedRecipe");
		super.deserializeNBT(nbt);
		if(isEmpty())
			appliedRecipe = false;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource) {
		return slot == 0 && isEmpty();
	}

}
