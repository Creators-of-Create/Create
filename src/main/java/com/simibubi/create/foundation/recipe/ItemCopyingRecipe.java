package com.simibubi.create.foundation.recipe;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllRecipeTypes;

import net.createmod.catnip.data.IntAttached;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class ItemCopyingRecipe extends CustomRecipe {

	public static interface SupportsItemCopying {

		public default ItemStack createCopy(ItemStack original, int count) {
			ItemStack copyWithCount = original.copyWithCount(count);
			copyWithCount.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
			copyWithCount.remove(DataComponents.STORED_ENCHANTMENTS);
			return copyWithCount;
		}

		public default boolean canCopyFromItem(ItemStack item) {
			return item.has(getComponentType());
		}

		public default boolean canCopyToItem(ItemStack item) {
			return !item.has(getComponentType());
		}

		public DataComponentType<?> getComponentType();

	}

	public ItemCopyingRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return copyCheck(input) != null;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		IntAttached<ItemStack> copyCheck = copyCheck(input);
		if (copyCheck == null)
			return ItemStack.EMPTY;

		ItemStack itemToCopy = copyCheck.getValue();
		if (!(itemToCopy.getItem() instanceof SupportsItemCopying sic))
			return ItemStack.EMPTY;

		return sic.createCopy(itemToCopy, copyCheck.getFirst() + 1);
	}

	@Nullable
	private IntAttached<ItemStack> copyCheck(CraftingInput input) {
		ItemStack itemToCopy = ItemStack.EMPTY;
		int copyTargets = 0;

		for (int j = 0; j < input.size(); ++j) {
			ItemStack itemInSlot = input.getItem(j);
			if (itemInSlot.isEmpty())
				continue;
			if (!itemToCopy.isEmpty() && itemToCopy.getItem() != itemInSlot.getItem())
				return null;
			if (!(itemInSlot.getItem() instanceof SupportsItemCopying sic))
				continue;

			if (sic.canCopyFromItem(itemInSlot)) {
				if (!itemToCopy.isEmpty())
					return null;
				itemToCopy = itemInSlot;
				continue;
			}

			if (sic.canCopyToItem(itemInSlot))
				copyTargets++;
		}

		if (itemToCopy.isEmpty() || copyTargets == 0)
			return null;

		return IntAttached.with(copyTargets, itemToCopy);
	}

	public RecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.ITEM_COPYING.getSerializer();
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width >= 2 && height >= 2;
	}
}
