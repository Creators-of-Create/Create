package com.simibubi.create.content.logistics.block.inventories;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllContraptionStorages;
import com.simibubi.create.api.contraption.ContraptionItemStackHandler;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemHandlerHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ContraptionItemStackHandler {

	private Supplier<ItemStack> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = suppliedItemStack.get();
		if (slot == 1)
			return ItemStack.EMPTY;
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
		return stack;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack stack = suppliedItemStack.get();
		if (slot == 1)
			return ItemStack.EMPTY;
		if (stack == null)
			return ItemStack.EMPTY;
		if (!stack.isEmpty())
			return ItemHandlerHelper.copyStackWithSize(stack, Math.min(stack.getMaxStackSize(), amount));
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		nbt.put("ProvidedStack", suppliedItemStack.get().serializeNBT());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		ItemStack stack = ItemStack.of(nbt.getCompound("ProvidedStack"));
		suppliedItemStack = () -> stack;
	}

	@Override
	public boolean addStorageToWorld(TileEntity te) {
		return false;
	}

	@Override
	public int getPriority() {
		return ContraptionItemStackHandler.PRIORITY_TRASH;
	}

	@Override
	protected ContraptionStorageRegistry registry() {
		return AllContraptionStorages.CREATIVE_CRATE.get();
	}
}
