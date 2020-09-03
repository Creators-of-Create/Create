package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHelper {

	public static void dropContents(World world, BlockPos pos, IItemHandler inv) {
		for (int slot = 0; slot < inv.getSlots(); slot++)
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
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
		return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
	}

	public static List<Pair<Ingredient, MutableInt>> condenseIngredients(NonNullList<Ingredient> recipeIngredients) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = new ArrayList<>();
		Ingredients: for (Ingredient igd : recipeIngredients) {
			for (Pair<Ingredient, MutableInt> pair : actualIngredients) {
				ItemStack[] stacks1 = pair.getFirst()
					.getMatchingStacks();
				ItemStack[] stacks2 = igd.getMatchingStacks();
				if (stacks1.length != stacks2.length)
					continue;
				for (int i = 0; i <= stacks1.length; i++) {
					if (i == stacks1.length) {
						pair.getSecond()
							.increment();
						continue Ingredients;
					}
					if (!ItemStack.areItemsEqual(stacks1[i], stacks2[i]))
						break;
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

	public static enum ExtractionCountMode {
		EXACTLY, UPTO
	}

	public static ItemStack extract(IItemHandler inv, Predicate<ItemStack> test, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.UPTO, AllConfigs.SERVER.logistics.extractorAmount.get(),
			simulate);
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
				ItemStack stack = inv.extractItem(slot, maxExtractionCount - extracting.getCount(), true);

				if (stack.isEmpty())
					continue;
				if (!test.test(stack))
					continue;
				if (!extracting.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, extracting)) {
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
		int maxExtractionCount = AllConfigs.SERVER.logistics.extractorAmount.get();

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			if (extracting.isEmpty()) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;
				int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
				if (maxExtractionCountForItem == 0)
					continue;
				maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
			}

			ItemStack stack = inv.extractItem(slot, maxExtractionCount - extracting.getCount(), true);

			if (!test.test(stack))
				continue;
			if (!extracting.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, extracting))
				continue;

			if (extracting.isEmpty())
				extracting = stack.copy();
			else
				extracting.grow(stack.getCount());

			if (!simulate)
				inv.extractItem(slot, stack.getCount(), false);
			if (extracting.getCount() == maxExtractionCount)
				break;
		}

		return extracting;
	}

}
