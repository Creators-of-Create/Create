package com.simibubi.create.content.logistics.item.filter;

import com.simibubi.create.AllContainerTypes;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class FilterContainer extends AbstractFilterContainer {

	boolean respectNBT;
	boolean blacklist;

	public FilterContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public FilterContainer(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
		super(type, id, inv, stack);
	}

	public static FilterContainer create(int id, Inventory inv, ItemStack stack) {
		return new FilterContainer(AllContainerTypes.FILTER.get(), id, inv, stack);
	}

	@Override
	protected int getPlayerInventoryXOffset() {
		return 38;
	}

	@Override
	protected int getPlayerInventoryYOffset() {
		return 119;
	}

	@Override
	protected void addFilterSlots() {
		int x = 23;
		int y = 20;
		for (int row = 0; row < 2; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new SlotItemHandler(ghostInventory, col + row * 9, x + col * 18, y + row * 18));
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return FilterItem.getFilterItems(contentHolder);
	}

	@Override
	protected void initAndReadInventory(ItemStack filterItem) {
		super.initAndReadInventory(filterItem);
		CompoundTag tag = filterItem.getOrCreateTag();
		respectNBT = tag.getBoolean("RespectNBT");
		blacklist = tag.getBoolean("Blacklist");
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		super.saveData(filterItem);
		CompoundTag tag = filterItem.getOrCreateTag();
		tag.putBoolean("RespectNBT", respectNBT);
		tag.putBoolean("Blacklist", blacklist);
	}

}
