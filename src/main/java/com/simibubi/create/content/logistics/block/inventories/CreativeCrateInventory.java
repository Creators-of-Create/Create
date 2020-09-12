package com.simibubi.create.content.logistics.block.inventories;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CreativeCrateInventory extends ItemStackHandler {

	private ItemStack filter = null;
	private final CreativeCrateTileEntity te;

	public CreativeCrateInventory(@Nullable CreativeCrateTileEntity te) {
		this.te = te;
	}

	public CreativeCrateInventory() {
		this(null);
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot == 1)
			return ItemStack.EMPTY;
		if (getFilter() == null)
			return ItemStack.EMPTY;
		if (!getFilter().isEmpty())
			filter.setCount(filter.getMaxStackSize());
		return filter;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (getFilter() == null)
			return ItemStack.EMPTY;
		if (!getFilter().isEmpty())
			filter.setCount(Math.min(getFilter().getMaxStackSize(), amount));
		return getFilter();
	}

	@Override
	public int getSlotLimit(int slot) {
		return getStackInSlot(slot).getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putBoolean("isCreativeCrate", true);
		if (getFilter() != null)
			ItemStackHelper.saveAllItems(nbt, NonNullList.from(ItemStack.EMPTY, getFilter()));
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		NonNullList<ItemStack> filterList = NonNullList.withSize(1, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, filterList);
		filter = filterList.get(0);
	}

	@Nullable
	public ItemStack getFilter() {
		if (te != null)
			filter = te.filter.getFilter();
		return filter;
	}
}
