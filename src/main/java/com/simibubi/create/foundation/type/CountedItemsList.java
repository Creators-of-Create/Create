package com.simibubi.create.foundation.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CountedItemsList {

	Map<Item, Set<ItemStackEntry>> items = new HashMap<>();

	Collection<ItemStackEntry> flattenedList = new PriorityQueue<>();
	boolean flattenedListDirty = true;

	public CountedItemsList() {
	}

	public CountedItemsList(IItemHandler inventory) {
		for (int slot = 0; slot < inventory.getSlots(); slot++)
			add(inventory.extractItem(slot, inventory.getSlotLimit(slot), true));
	}

	public List<ItemStackEntry> getStacksToUpdate(CountedItemsList newList) {
		List<ItemStackEntry> changes = new ArrayList<>();
		for (Item key : Sets.union(items.keySet(), newList.items.keySet())) {
			Set<ItemStackEntry> currentSet = items.get(key);
			Set<ItemStackEntry> newSet = newList.items.get(key);

			if (currentSet == null) {
				changes.addAll(newSet);
				continue;
			}
			if (newSet == null) {
				currentSet.forEach(entry -> changes.add(new ItemStackEntry(entry.stack, 0)));
				continue;
			}

			Set<ItemStackEntry> remainderNew = new HashSet<>(newSet);
			OuterLoop: for (ItemStackEntry entry : currentSet) {
				for (ItemStackEntry newEntry : newSet) {
					if (!entry.matches(newEntry.stack))
						continue;
					remainderNew.remove(newEntry);
					if (entry.amount != newEntry.amount)
						changes.add(newEntry);
					continue OuterLoop;
				}
				changes.add(new ItemStackEntry(entry.stack, 0));
			}
			changes.addAll(remainderNew);
		}

		return changes;
	}

	public void add(ItemStack stack) {
		add(stack, stack.getCount());
	}

	public void add(ItemStackEntry entry) {
		add(entry.stack, entry.amount);
	}

	public void add(ItemStack stack, int amount) {
		if (stack.isEmpty())
			return;
		
		Set<ItemStackEntry> stackSet = getOrCreateItemSet(stack);
		for (ItemStackEntry entry : stackSet) {
			if (!entry.matches(stack))
				continue;
			entry.amount += amount;
			return;
		}
		stackSet.add(new ItemStackEntry(stack, amount));
		flattenedListDirty = true;
	}

	public boolean contains(ItemStack stack) {
		return getItemCount(stack) != 0;
	}

	public int getItemCount(ItemStack stack) {
		Set<ItemStackEntry> stackSet = getItemSet(stack);
		if (stackSet == null)
			return 0;
		for (ItemStackEntry entry : stackSet) {
			if (!entry.matches(stack))
				continue;
			return entry.amount;
		}
		return 0;
	}

	public void setItemCount(ItemStack stack, int amount) {
		remove(stack);
		add(stack, amount);
	}

	public void remove(ItemStack stack) {
		Set<ItemStackEntry> stackSet = getItemSet(stack);
		if (stackSet == null)
			return;

		for (Iterator<ItemStackEntry> iterator = stackSet.iterator(); iterator.hasNext();) {
			ItemStackEntry entry = iterator.next();
			if (entry.matches(stack)) {
				iterator.remove();
				flattenedListDirty = true;
				return;
			}
		}
	}

	public Collection<ItemStackEntry> getFlattenedList() {
		if (flattenedListDirty) {
			flattenedList.clear();
			items.values().forEach(set -> flattenedList.addAll(set));
			flattenedListDirty = false;
		}
		return flattenedList;
	}

	private Set<ItemStackEntry> getItemSet(ItemStack stack) {
		return items.get(stack.getItem());
	}

	private Set<ItemStackEntry> getOrCreateItemSet(ItemStack stack) {
		if (!items.containsKey(stack.getItem()))
			items.put(stack.getItem(), new HashSet<>());
		return getItemSet(stack);
	}

	public class ItemStackEntry implements Comparable<ItemStackEntry> {
		public ItemStack stack;
		public int amount;

		public ItemStackEntry(ItemStack stack) {
			this(stack, stack.getCount());
		}

		public ItemStackEntry(CompoundNBT nbt) {
			this(ItemStack.read(nbt.getCompound("Item")), nbt.getInt("Amount"));
		}

		public ItemStackEntry(ItemStack stack, int amount) {
			this.stack = stack.copy();
			this.amount = amount;
		}

		public boolean matches(ItemStack other) {
			return ItemHandlerHelper.canItemStacksStack(other, stack);
		}

		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Item", stack.serializeNBT());
			nbt.putInt("Amount", amount);
			return nbt;
		}

		@Override
		public int compareTo(ItemStackEntry o) {
			return amount - o.amount;
		}

	}

}
