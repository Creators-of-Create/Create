package com.simibubi.create.foundation.utility;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class VisitedItemStackTracker {
	Map<AbstractItemStack, SlotAmountRecord> itemStackMap = new HashMap<>();

	public SlotAmountRecord update(ItemStack itemStack, int slot) {
		SlotAmountRecord record = itemStackMap.computeIfAbsent(new AbstractItemStack(itemStack), k -> new SlotAmountRecord());
		record.add(slot, itemStack.getCount());
		return record;
	}

	public static class AbstractItemStack {
		private final ItemStack stack;
		private boolean initialized = false;
		private int hashCode;

		AbstractItemStack(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int hashCode() {
			if (!initialized) {
				initialized = true;
				hashCode = stack.getItem().hashCode();
				hashCode = hashCode * 31 + stack.getComponents().hashCode();
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof AbstractItemStack otherStack))
				return false;
			return ItemStack.isSameItemSameComponents(stack, otherStack.stack);
		}
	}

	public static class SlotAmountRecord {
		public final IntArrayList slots = new IntArrayList();
		public int totalAmount = 0;

		public void add(int slot, int amount) {
			slots.add(slot);
			totalAmount += amount;
		}
	}
}
