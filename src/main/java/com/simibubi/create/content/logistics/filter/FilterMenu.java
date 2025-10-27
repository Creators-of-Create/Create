package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllMenuTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FilterMenu extends AbstractFilterMenu {

	boolean respectNBT;
	boolean blacklist;

	public FilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public FilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
		super(type, id, inv, stack);
	}

	public static FilterMenu create(int id, Inventory inv, ItemStack stack) {
		return new FilterMenu(AllMenuTypes.FILTER.get(), id, inv, stack);
	}

	@Override
	protected int getPlayerInventoryXOffset() {
		return 38;
	}

	@Override
	protected int getPlayerInventoryYOffset() {
		return 121;
	}

	@Override
	protected void addFilterSlots() {
		int x = 23;
		int y = 25;
		for (int row = 0; row < 2; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new SlotItemHandler(ghostInventory, col + row * 9, x + col * 18, y + row * 18));
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return AllItems.FILTER.get().getFilterItemHandler(contentHolder);
	}

	@Override
	protected void initAndReadInventory(ItemStack filterItem) {
		super.initAndReadInventory(filterItem);
		respectNBT = filterItem.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
		blacklist = filterItem.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		super.saveData(filterItem);
		filterItem.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, respectNBT);
		filterItem.set(AllDataComponents.FILTER_ITEMS_BLACKLIST, blacklist);

		if (respectNBT || blacklist)
			return;
		for (int i = 0; i < ghostInventory.getSlots(); i++)
			if (!ghostInventory.getStackInSlot(i)
				.isEmpty())
				return;
		filterItem.remove(AllDataComponents.FILTER_ITEMS_RESPECT_NBT);
		filterItem.remove(AllDataComponents.FILTER_ITEMS_BLACKLIST);
	}

}
