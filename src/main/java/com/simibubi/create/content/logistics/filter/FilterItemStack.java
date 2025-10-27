package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FilterItemStack {
	private final ItemStack filterItemStack;
	private boolean fluidExtracted;
	private FluidStack filterFluidStack;

	public static FilterItemStack of(ItemStack filter) {
		if (!filter.isComponentsPatchEmpty() && filter.getItem() instanceof FilterItem item) {
			trimFilterComponents(filter);
			return item.makeStackWrapper(filter);
		}

		return new FilterItemStack(filter);
	}

	public static FilterItemStack of(HolderLookup.Provider registries, CompoundTag tag) {
		return of(ItemStack.parseOptional(registries, tag));
	}

	public static FilterItemStack empty() {
		return of(ItemStack.EMPTY);
	}

	private static void trimFilterComponents(ItemStack filter) {
		filter.remove(DataComponents.ENCHANTMENTS);
		filter.remove(DataComponents.ATTRIBUTE_MODIFIERS);
	}

	public boolean isEmpty() {
		return filterItemStack.isEmpty();
	}

	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		return (CompoundTag) filterItemStack.saveOptional(registries);
	}

	public ItemStack item() {
		return filterItemStack;
	}

	public FluidStack fluid(Level level) {
		resolveFluid(level);
		return filterFluidStack;
	}

	public boolean isFilterItem() {
		return filterItemStack.getItem() instanceof FilterItem;
	}

	//

	public boolean test(Level world, ItemStack stack) {
		return test(world, stack, false);
	}

	public boolean test(Level world, FluidStack stack) {
		return test(world, stack, true);
	}

	public boolean test(Level world, ItemStack stack, boolean matchNBT) {
		if (isEmpty())
			return true;
		return FilterItem.testDirect(filterItemStack, stack, matchNBT);
	}

	public boolean test(Level world, FluidStack stack, boolean matchNBT) {
		if (isEmpty())
			return true;
		if (stack.isEmpty())
			return false;

		resolveFluid(world);

		if (filterFluidStack.isEmpty())
			return false;
		if (!matchNBT)
			return filterFluidStack.getFluid()
				.isSame(stack.getFluid());
		return net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(filterFluidStack, stack);
	}

	//

	private void resolveFluid(Level world) {
		if (!fluidExtracted) {
			fluidExtracted = true;
			if (GenericItemEmptying.canItemBeEmptied(world, filterItemStack))
				filterFluidStack = GenericItemEmptying.emptyItem(world, filterItemStack, true)
					.getFirst();
		}
	}

	protected FilterItemStack(ItemStack filter) {
		filterItemStack = filter;
		filterFluidStack = FluidStack.EMPTY;
		fluidExtracted = false;
	}

	public static class ListFilterItemStack extends FilterItemStack {

		public List<FilterItemStack> containedItems;
		public boolean shouldRespectNBT;
		public boolean isBlacklist;

		public ListFilterItemStack(ItemStack filter) {
			super(filter);
			boolean hasFilterItems = filter.has(AllDataComponents.FILTER_ITEMS);

			containedItems = new ArrayList<>();
			ItemStackHandler items = ((ListFilterItem) filter.getItem()).getFilterItemHandler(filter);
			for (int i = 0; i < items.getSlots(); i++) {
				ItemStack stackInSlot = items.getStackInSlot(i);
				if (!stackInSlot.isEmpty())
					containedItems.add(FilterItemStack.of(stackInSlot));
			}

			shouldRespectNBT = hasFilterItems && filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
			isBlacklist = hasFilterItems && filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
		}

		@Override
		public boolean test(Level world, ItemStack stack, boolean matchNBT) {
			for (FilterItemStack filterItemStack : containedItems)
				if (filterItemStack.test(world, stack, shouldRespectNBT))
					return !isBlacklist;
			return isBlacklist;
		}

		@Override
		public boolean test(Level world, FluidStack stack, boolean matchNBT) {
			for (FilterItemStack filterItemStack : containedItems)
				if (filterItemStack.test(world, stack, shouldRespectNBT))
					return !isBlacklist;
			return isBlacklist;
		}

	}

	public static class AttributeFilterItemStack extends FilterItemStack {
		public AttributeFilterWhitelistMode whitelistMode;
		public List<Pair<ItemAttribute, Boolean>> attributeTests;

		public AttributeFilterItemStack(ItemStack filter) {
			super(filter);
			boolean defaults = !filter.has(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);

			attributeTests = new ArrayList<>();
			whitelistMode = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);

			List<ItemAttribute.ItemAttributeEntry> attributes = defaults ? new ArrayList<>()
				: filter.get(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);
			//noinspection DataFlowIssue
			for (ItemAttribute.ItemAttributeEntry attributeEntry : attributes) {
				ItemAttribute attribute = attributeEntry.attribute();
				if (attribute != null)
					attributeTests.add(Pair.of(attribute, attributeEntry.inverted()));
			}
		}

		@Override
		public boolean test(Level world, FluidStack stack, boolean matchNBT) {
			return false;
		}

		@Override
		public boolean test(Level world, ItemStack stack, boolean matchNBT) {
			if (attributeTests.isEmpty())
				return super.test(world, stack, matchNBT);
			for (Pair<ItemAttribute, Boolean> test : attributeTests) {
				ItemAttribute attribute = test.getFirst();
				boolean inverted = test.getSecond();
				boolean matches = attribute.appliesTo(stack, world) != inverted;

				if (matches) {
					switch (whitelistMode) {
						case BLACKLIST -> {
							return false;
						}
						case WHITELIST_CONJ -> {
							continue;
						}
						case WHITELIST_DISJ -> {
							return true;
						}
					}
				} else {
					switch (whitelistMode) {
						case BLACKLIST, WHITELIST_DISJ -> {
							continue;
						}
						case WHITELIST_CONJ -> {
							return false;
						}
					}
				}
			}

			return switch (whitelistMode) {
				case BLACKLIST, WHITELIST_CONJ -> true;
				case WHITELIST_DISJ -> false;
			};

		}

	}

	public static class PackageFilterItemStack extends FilterItemStack {

		public String filterString;

		public PackageFilterItemStack(ItemStack filter) {
			super(filter);
			filterString = PackageItem.getAddress(filter);
		}

		@Override
		public boolean test(Level world, ItemStack stack, boolean matchNBT) {
			return (filterString.isBlank() && super.test(world, stack, matchNBT))
				|| PackageItem.isPackage(stack) && PackageItem.matchAddress(stack, filterString);
		}

		@Override
		public boolean test(Level world, FluidStack stack, boolean matchNBT) {
			return false;
		}

	}
}
