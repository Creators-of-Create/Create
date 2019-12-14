package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHelper {

	public static List<ItemStack> multipliedOutput(ItemStack in, ItemStack out) {
		List<ItemStack> stacks = new ArrayList<>();
		ItemStack result = out.copy();
		result.setCount(in.getCount() * out.getCount());

		while (result.getCount() > result.getMaxStackSize()) {
			stacks.add(result.split(result.getMaxStackSize()));
		}

		stacks.add(result);
		return stacks;
	}

	public static void addToList(ItemStack stack, List<ItemStack> stacks) {
		for (ItemStack s : stacks) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, s))
				continue;
			int transferred = Math.min(s.getMaxStackSize() - s.getCount(), stack.getCount());
			s.grow(transferred);
			stack.shrink(transferred);
		}
		if (stack.getCount() > 0)
			stacks.add(stack);
	}

	public static boolean isSameInventory(IItemHandler h1, IItemHandler h2) {
		if (h1 == null || h2 == null)
			return false;
		if (h1.getSlots() != h2.getSlots())
			return false;
		for (int slot = 0; slot < h1.getSlots(); slot++) {
			if (h1.getStackInSlot(slot) != h2.getStackInSlot(slot))
				return false;
		}
		return true;
	}

	public static List<Pair<Ingredient, MutableInt>> condenseIngredients(NonNullList<Ingredient> recipeIngredients) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = new ArrayList<>();
		Ingredients: for (Ingredient igd : recipeIngredients) {
			for (Pair<Ingredient, MutableInt> pair : actualIngredients) {
				ItemStack[] stacks1 = pair.getKey().getMatchingStacks();
				ItemStack[] stacks2 = igd.getMatchingStacks();
				if (stacks1.length == stacks2.length) {
					for (int i = 0; i <= stacks1.length; i++) {
						if (i == stacks1.length) {
							pair.getValue().increment();
							continue Ingredients;
						}
						if (!ItemStack.areItemsEqual(stacks1[i], stacks2[i]))
							break;
					}
				}
			}
			actualIngredients.add(Pair.of(igd, new MutableInt(1)));
		}
		return actualIngredients;
	}

	public static boolean matchIngredients(Ingredient i1, Ingredient i2) {
		ItemStack[] stacks1 = i1.getMatchingStacks();
		ItemStack[] stacks2 = i2.getMatchingStacks();
		if (stacks1.length == stacks2.length) {
			for (int i = 0; i < stacks1.length; i++)
				if (!ItemStack.areItemsEqual(stacks1[i], stacks2[i]))
					return false;
			return true;
		}
		return false;
	}

}
