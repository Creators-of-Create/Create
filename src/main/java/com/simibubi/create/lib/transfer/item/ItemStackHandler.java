package com.simibubi.create.lib.transfer.item;

import java.util.Arrays;

import com.simibubi.create.lib.util.NBTSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ItemStackHandler implements IItemHandlerModifiable, NBTSerializable {
	protected ItemStack[] stacks;

	public ItemStackHandler(int stacks) {
		this.stacks = new ItemStack[stacks];
		Arrays.fill(this.stacks, ItemStack.EMPTY);
	}

	public ItemStackHandler() {
		this(1);
	}

	public void setSize(int size) {
		stacks = new ItemStack[size];
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	protected void onContentsChanged(int slot) {}

	// IItemHandler

	@Override
	public int getSlots() {
		return stacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return stacks[slot];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
		if (stack.isEmpty()) return ItemStack.EMPTY;
		if (!isItemValid(slot, stack)) return stack;

		ItemStack oldStack = getStackInSlot(slot);
		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

		if (!oldStack.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, oldStack)) return stack;
			limit -= oldStack.getCount();
		}
		if (limit <= 0) return stack;

		boolean limitReached = stack.getCount() > limit;

		if (!sim) {
			if (oldStack.isEmpty()) {
				setStackInSlot(slot, limitReached ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			} else {
				oldStack.grow(limitReached ? limit : stack.getCount());
			}
			onContentsChanged(slot);
		}
		return limitReached ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean sim) {
		if (amount == 0) return ItemStack.EMPTY;

		ItemStack oldStack = getStackInSlot(slot);
		if (oldStack.isEmpty()) return ItemStack.EMPTY;

		int toExtract = Math.min(amount, oldStack.getMaxStackSize());

		if (oldStack.getCount() <= toExtract) {
			if (!sim) {
				setStackInSlot(slot, ItemStack.EMPTY);
				onContentsChanged(slot);
				return oldStack;
			} else {
				return oldStack.copy();
			}
		} else {
			if (!sim) {
				setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(oldStack, oldStack.getCount() - toExtract));
				onContentsChanged(slot);
			}

			return ItemHandlerHelper.copyStackWithSize(oldStack, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	// Modifiable

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		stacks[slot] = stack;
	}

	// NBTSerializable

	@Override
	public CompoundTag create$serializeNBT() {
		ListTag nbtTagList = new ListTag();
		for (int i = 0; i < stacks.length; i++) {
			if (!stacks[i].isEmpty()) {
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("Slot", i);
				stacks[i].save(itemTag);
				nbtTagList.add(itemTag);
			}
		}
		CompoundTag nbt = new CompoundTag();
		nbt.put("Items", nbtTagList);
		nbt.putInt("Size", stacks.length);
		return nbt;
	}

	public CompoundTag serializeNBT() {
		return create$serializeNBT();
	}

	@Override
	public void create$deserializeNBT(CompoundTag nbt) {
		int size = nbt.getInt("Size");
		setSize(size);
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag itemTags = tagList.getCompound(i);
			int slot = itemTags.getInt("Slot");

			if (slot >= 0 && slot < stacks.length) {
				stacks[slot] = ItemStack.of(itemTags);
			}
		}
	}

	public void deserializeNBT(CompoundTag tag) {
		create$deserializeNBT(tag);
	}
}
