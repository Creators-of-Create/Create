package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHelper {

	public static void dropContents(Level world, BlockPos pos, IItemHandler inv) {
		for (int slot = 0; slot < inv.getSlots(); slot++)
			Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
	}

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

	public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null)
			return 0;
		int i = 0;
		float f = 0.0F;
		int totalSlots = inv.getSlots();

		for (int j = 0; j < inv.getSlots(); ++j) {
			int slotLimit = inv.getSlotLimit(j);
			if (slotLimit == 0) {
				totalSlots--;
				continue;
			}
			ItemStack itemstack = inv.getStackInSlot(j);
			if (!itemstack.isEmpty()) {
				f += (float) itemstack.getCount() / (float) Math.min(slotLimit, itemstack.getMaxStackSize());
				++i;
			}
		}

		if (totalSlots == 0)
			return 0;

		f = f / totalSlots;
		return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
	}

	public static List<Pair<Ingredient, MutableInt>> condenseIngredients(NonNullList<Ingredient> recipeIngredients) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = new ArrayList<>();
		Ingredients: for (Ingredient igd : recipeIngredients) {
			for (Pair<Ingredient, MutableInt> pair : actualIngredients) {
				ItemStack[] stacks1 = pair.getFirst()
					.getItems();
				ItemStack[] stacks2 = igd.getItems();
				if (stacks1.length != stacks2.length)
					continue;
				for (int i = 0; i <= stacks1.length; i++) {
					if (i == stacks1.length) {
						pair.getSecond()
							.increment();
						continue Ingredients;
					}
					if (!ItemStack.matches(stacks1[i], stacks2[i]))
						break;
				}
			}
			actualIngredients.add(Pair.of(igd, new MutableInt(1)));
		}
		return actualIngredients;
	}

	public static boolean matchIngredients(Ingredient i1, Ingredient i2) {
		if (i1 == i2)
			return true;
		ItemStack[] stacks1 = i1.getItems();
		ItemStack[] stacks2 = i2.getItems();
		if (stacks1 == stacks2)
			return true;
		if (stacks1.length == stacks2.length) {
			for (int i = 0; i < stacks1.length; i++)
				if (!ItemStack.isSameItem(stacks1[i], stacks2[i]))
					return false;
			return true;
		}
		return false;
	}

	public static boolean matchAllIngredients(NonNullList<Ingredient> ingredients) {
		if (ingredients.size() <= 1)
			return true;
		Ingredient firstIngredient = ingredients.get(0);
		for (int i = 1; i < ingredients.size(); i++)
			if (!matchIngredients(firstIngredient, ingredients.get(i)))
				return false;
		return true;
	}

	public static enum ExtractionCountMode {
		EXACTLY, UPTO
	}

	public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.UPTO, 64, simulate);
	}

	public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, int exactAmount, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
	}

	public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, ExtractionCountMode mode, int amount,
		boolean simulate) {
		ItemStack extracting = ItemStack.EMPTY;
		boolean amountRequired = mode == ExtractionCountMode.EXACTLY;
		boolean checkHasEnoughItems = amountRequired;
		boolean hasEnoughItems = !checkHasEnoughItems;
		boolean potentialOtherMatch = false;
		int maxExtractionCount = amount;

		Extraction: do {
			extracting = ItemStack.EMPTY;

			for (int slot = 0; slot < inv.getSlots(); slot++) {
				int amountToExtractFromThisSlot =
					Math.min(maxExtractionCount - extracting.getCount(), inv.getStackInSlot(slot)
						.getMaxStackSize());
				ItemStack stack = inv.extractItem(slot, amountToExtractFromThisSlot, true);

				if (stack.isEmpty())
					continue;
				if (!test.test(stack))
					continue;
				if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting)) {
					potentialOtherMatch = true;
					continue;
				}

				if (extracting.isEmpty())
					extracting = stack.copy();
				else
					extracting.grow(stack.getCount());

				if (!simulate && hasEnoughItems)
					inv.extractItem(slot, stack.getCount(), false);

				if (extracting.getCount() >= maxExtractionCount) {
					if (checkHasEnoughItems) {
						hasEnoughItems = true;
						checkHasEnoughItems = false;
						continue Extraction;
					} else {
						break Extraction;
					}
				}
			}

			if (!extracting.isEmpty() && !hasEnoughItems && potentialOtherMatch) {
				ItemStack blackListed = extracting.copy();
				test = test.and(i -> !ItemHandlerHelper.canItemStacksStack(i, blackListed));
				continue;
			}

			if (checkHasEnoughItems)
				checkHasEnoughItems = false;
			else
				break Extraction;

		} while (true);

		if (amountRequired && extracting.getCount() < amount)
			return ItemStack.EMPTY;

		return extracting;
	}

	public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test,
		Function<ItemStack, Integer> amountFunction, boolean simulate) {
		ItemStack extracting = ItemStack.EMPTY;
		int maxExtractionCount = 64;

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			if (extracting.isEmpty()) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot.isEmpty() || !test.test(stackInSlot))
					continue;
				int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
				if (maxExtractionCountForItem == 0)
					continue;
				maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
			}

			ItemStack stack = inv.extractItem(slot, maxExtractionCount - extracting.getCount(), true);

			if (!test.test(stack))
				continue;
			if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting))
				continue;

			if (extracting.isEmpty())
				extracting = stack.copy();
			else
				extracting.grow(stack.getCount());

			if (!simulate)
				inv.extractItem(slot, stack.getCount(), false);
			if (extracting.getCount() >= maxExtractionCount)
				break;
		}

		return extracting;
	}

	public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
		return ItemHandlerHelper.canItemStacksStack(a, b) && a.getCount() + b.getCount() <= a.getMaxStackSize();
	}

	public static ItemStack findFirstMatch(IItemHandler inv, Predicate<ItemStack> test) {
		int slot = findFirstMatchingSlotIndex(inv, test);
		if (slot == -1)
			return ItemStack.EMPTY;
		else
			return inv.getStackInSlot(slot);
	}

	public static int findFirstMatchingSlotIndex(IItemHandler inv, Predicate<ItemStack> test) {
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack toTest = inv.getStackInSlot(slot);
			if (test.test(toTest))
				return slot;
		}
		return -1;
	}
}
