package com.simibubi.create.content.contraptions.mounted;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemHandlerModifiableFromIInventory implements IItemHandlerModifiable {
	private final Container inventory;

	public ItemHandlerModifiableFromIInventory(Container inventory) {
		this.inventory = inventory;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inventory.setItem(slot, stack);
	}

	@Override
	public int getSlots() {
		return inventory.getContainerSize();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getItem(slot);
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		if (!isItemValid(slot, stack))
			return stack;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		int limit = getStackLimit(slot, stack);

		if (!existing.isEmpty())
		{
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
				return stack;

			limit -= existing.getCount();
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate)
		{
			if (existing.isEmpty())
			{
				setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			}
			else
			{
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		if (existing.isEmpty())
			return ItemStack.EMPTY;

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (existing.getCount() <= toExtract)
		{
			if (!simulate)
			{
				setStackInSlot(slot, ItemStack.EMPTY);
				return existing;
			}
			else
			{
				return existing.copy();
			}
		}
		else
		{
			if (!simulate)
			{
				setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
			}

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inventory.canPlaceItem(slot, stack);
	}

	private void validateSlotIndex(int slot)
	{
		if (slot < 0 || slot >= getSlots())
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	private int getStackLimit(int slot, ItemStack stack)
	{
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}
}
