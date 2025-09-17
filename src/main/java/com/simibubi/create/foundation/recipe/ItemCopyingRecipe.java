package com.simibubi.create.foundation.recipe;

import javax.annotation.Nullable;

import com.simibubi.create.AllRecipeTypes;

import net.createmod.catnip.data.IntAttached;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ItemCopyingRecipe extends CustomRecipe {

	public static interface SupportsItemCopying {

		public default ItemStack createCopy(ItemStack original, int count) {
			ItemStack copyWithCount = original.copyWithCount(count);
			copyWithCount.removeTagKey("Enchantments");
			return copyWithCount;
		}

		public default boolean canCopyFromItem(ItemStack item) {
			return item.hasTag();
		}

		public default boolean canCopyToItem(ItemStack item) {
			return !item.hasTag();
		}

	}

	public ItemCopyingRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		return copyCheck(inv) != null;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		IntAttached<ItemStack> copyCheck = copyCheck(container);
		if (copyCheck == null)
			return ItemStack.EMPTY;

		ItemStack itemToCopy = copyCheck.getValue();
		if (!(itemToCopy.getItem() instanceof SupportsItemCopying sic))
			return ItemStack.EMPTY;

		return sic.createCopy(itemToCopy, copyCheck.getFirst() + 1);
	}

	@Nullable
	private IntAttached<ItemStack> copyCheck(CraftingContainer inv) {
		ItemStack itemToCopy = ItemStack.EMPTY;
		int copyTargets = 0;

		for (int j = 0; j < inv.getContainerSize(); ++j) {
			ItemStack itemInSlot = inv.getItem(j);
			if (itemInSlot.isEmpty())
				continue;
			if (!(itemInSlot.getItem() instanceof SupportsItemCopying sic))
				return null;
			if (!sic.canCopyFromItem(itemInSlot))
				continue;
			itemToCopy = itemInSlot;
			break;
		}
		if(itemToCopy.isEmpty())
			return null;

		for (int j = 0; j < inv.getContainerSize(); ++j) {
			ItemStack itemInSlot = inv.getItem(j);
			if (itemInSlot.isEmpty() || itemInSlot == itemToCopy)
				continue;
			if (itemToCopy.getItem() != itemInSlot.getItem())
				return null;
			if (!(itemInSlot.getItem() instanceof SupportsItemCopying sic))
				return null;
			if (sic.canCopyFromItem(itemInSlot))
				return null;
			if (!sic.canCopyToItem(itemInSlot))
				return null;
			copyTargets++;
		}
		if (copyTargets == 0)
			return null;

		return IntAttached.with(copyTargets, itemToCopy);
	}

	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		return nonnulllist;
	}

	public RecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.ITEM_COPYING.getSerializer();
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width >= 2 && height >= 2;
	}
}
