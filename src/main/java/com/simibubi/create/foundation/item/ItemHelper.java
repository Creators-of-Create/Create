package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Pair;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class ItemHelper {

	public static void dropContents(Level world, BlockPos pos, Storage<ItemVariant> inv) {
		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<ItemVariant> view : inv.iterable(t)) {
				ItemStack stack = view.getResource().toStack((int) view.getAmount());
				Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
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

//	public static boolean isSameInventory(Storage<ItemVariant> h1, Storage<ItemVariant> h2) {
//		if (h1 == null || h2 == null)
//			return false;
//		if (h1.getSlots() != h2.getSlots())
//			return false;
//		for (int slot = 0; slot < h1.getSlots(); slot++) {
//			if (h1.getStackInSlot(slot) != h2.getStackInSlot(slot))
//				return false;
//		}
//		return true;
//	}

	public static int calcRedstoneFromInventory(@Nullable Storage<ItemVariant> inv) {
		if (inv == null)
			return 0;
		int i = 0;
		float f = 0.0F;
		int totalSlots = 0;

		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<ItemVariant> view : inv.iterable(t)) {
				long slotLimit = view.getCapacity();
				if (slotLimit == 0) {
					continue;
				}
				totalSlots++;
				if (!view.isResourceBlank()) {
					f += (float) view.getAmount() / (float) Math.min(slotLimit, view.getResource().getItem().getMaxStackSize());
					++i;
				}
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
				if (!ItemStack.isSame(stacks1[i], stacks2[i]))
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

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.UPTO, AllConfigs.SERVER.logistics.defaultExtractionLimit.get(),
			simulate);
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, int exactAmount, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test, ExtractionCountMode mode, int amount,
		boolean simulate) {
		long extracted = 0;
		ItemVariant variant = null;
		if (inv.supportsExtraction()) {
			try (Transaction t = TransferUtil.getTransaction()) {
				for (StorageView<ItemVariant> view : inv.iterable(t)) {
					if (view.isResourceBlank()) continue;
					variant = variant == null ? view.getResource() : variant;
					if (!test.test(variant.toStack())) continue;
					long toExtract = Math.min(amount, view.getAmount());
					long actualExtracted = view.extract(variant, toExtract, t);
					if (actualExtracted == 0) continue;
					extracted += actualExtracted;
					if (extracted == amount) {
						if (!simulate)
							t.commit();
						return variant.toStack((int) extracted);
					}
				}

				// if the code reaches this point, we've extracted as much as possible, and it isn't enough.
				if (mode == ExtractionCountMode.UPTO) { // we don't need to get exactly the amount requested
					if (variant != null && extracted != 0) {
						if (!simulate) t.commit();
						return variant.toStack((int) extracted);
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack extract(Storage<ItemVariant> inv, Predicate<ItemStack> test,
		Function<ItemStack, Integer> amountFunction, boolean simulate) {
		ItemStack extracting = ItemStack.EMPTY;
		int maxExtractionCount = AllConfigs.SERVER.logistics.defaultExtractionLimit.get();

		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<ItemVariant> view : inv.iterable(t)) {
				if (view.isResourceBlank())
					continue;
				ItemVariant var = view.getResource();
				if (extracting.isEmpty()) {
					ItemStack stackInSlot = var.toStack();
					int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
					if (maxExtractionCountForItem == 0)
						continue;
					maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
				}

				long extracted = view.extract(var, maxExtractionCount - extracting.getCount(), t);
				ItemStack stack = var.toStack((int) extracted);

				if (!test.test(stack))
					continue;
				if (!extracting.isEmpty() && !canItemStackAmountsStack(stack, extracting))
					continue;

				if (extracting.isEmpty())
					extracting = stack.copy();
				else
					extracting.grow(stack.getCount());

				if (extracting.getCount() >= maxExtractionCount)
					break;
			}
			if (!simulate) t.commit();
		}

		return extracting;
	}

	public static boolean canItemStackAmountsStack(ItemStack a, ItemStack b) {
		return ItemHandlerHelper.canItemStacksStack(a, b) && a.getCount() + b.getCount() <= a.getMaxStackSize();
	}

//	public static ItemStack findFirstMatch(Storage<ItemVariant> inv, Predicate<ItemStack> test) {
//		int slot = findFirstMatchingSlotIndex(inv, test);
//		if (slot == -1)
//			return ItemStack.EMPTY;
//		else
//			return inv.getStackInSlot(slot);
//	}
}
