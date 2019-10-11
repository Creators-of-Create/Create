package com.simibubi.create.modules.logistics.management.base;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public abstract class LogisticalTask implements Comparable<LogisticalTask> {

	public enum Priority {
		HIGH, MEDIUM, LOW
	}

	Priority priority = Priority.LOW;
	public String targetAddress;

	@Override
	public int compareTo(LogisticalTask o) {
		return priority.compareTo(o.priority);
	}

	public static class SupplyTask extends LogisticalTask {
		public List<Pair<Ingredient, Integer>> items;
		private String display;

		public SupplyTask(ItemStackEntry requested, String address) {
			items = Arrays.asList(Pair.of(Ingredient.fromStacks(requested.stack), requested.amount));
			targetAddress = address;
			display = "Supply " + requested.amount + "x " + requested.stack.getItem().getName().getFormattedText()
					+ " -> " + address;
		}

		@Override
		public String toString() {
			return display;
		}
	}

	public static class DepositTask extends LogisticalTask {
		public ItemStack stack;

		public DepositTask(ItemStack stack) {
			this.stack = stack.copy();
		}
	}

}
