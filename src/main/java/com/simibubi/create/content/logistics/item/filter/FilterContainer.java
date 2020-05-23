package com.simibubi.create.content.logistics.item.filter;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FilterContainer extends AbstractFilterContainer {

	boolean respectNBT;
	boolean blacklist;

	public FilterContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainerTypes.FILTER.type, id, inv, extraData);
	}

	public FilterContainer(int id, PlayerInventory inv, ItemStack stack) {
		super(AllContainerTypes.FILTER.type, id, inv, stack);
	}

	@Override
	protected void addFilterSlots() {
		int x = 16;
		int y = 21;

		for (int row = 0; row < 2; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new SlotItemHandler(filterInventory, col + row * 9, x + col * 18, y + row * 18));
	}
	
	@Override
	protected ItemStackHandler createFilterInventory() {
		return FilterItem.getFilterItems(filterItem);
	}

	@Override
	protected int getInventoryOffset() {
		return 100;
	}
	
	@Override
	protected void readData(ItemStack filterItem) {
		CompoundNBT tag = filterItem.getOrCreateTag();
		respectNBT = tag.getBoolean("RespectNBT");
		blacklist = tag.getBoolean("Blacklist");
	}
	
	@Override
	protected void saveData(ItemStack filterItem) {
		CompoundNBT tag = filterItem.getOrCreateTag();
		tag.putBoolean("RespectNBT", respectNBT);
		tag.putBoolean("Blacklist", blacklist);
	}

}
