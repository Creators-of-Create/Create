package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.logistics.filter.FilterItemStack.ListFilterItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFilterItem extends FilterItem {
	protected ListFilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public List<Component> makeSummary(ItemStack filter) {
		if (!filter.hasTag()) return Collections.emptyList();

		List<Component> list = new ArrayList<>();

		ItemStackHandler filterItems = getFilterItemHandler(filter);
		boolean blacklist = filter.getOrCreateTag()
			.getBoolean("Blacklist");

		list.add((blacklist ? CreateLang.translateDirect("gui.filter.deny_list")
			: CreateLang.translateDirect("gui.filter.allow_list")).withStyle(ChatFormatting.GOLD));
		int count = 0;
		for (int i = 0; i < filterItems.getSlots(); i++) {
			if (count > 3) {
				list.add(Component.literal("- ...")
					.withStyle(ChatFormatting.DARK_GRAY));
				break;
			}

			ItemStack filterStack = filterItems.getStackInSlot(i);
			if (filterStack.isEmpty())
				continue;
			list.add(Component.literal("- ")
				.append(filterStack.getHoverName())
				.withStyle(ChatFormatting.GRAY));
			count++;
		}

		if (count == 0)
			return Collections.emptyList();

		return list;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return FilterMenu.create(id, inv, player.getMainHandItem());
	}

	@Override
	public FilterItemStack makeStackWrapper(ItemStack filter) {
		return new ListFilterItemStack(filter);
	}

	public ItemStackHandler getFilterItemHandler(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		CompoundTag invNBT = stack.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	@Override
	public ItemStack[] getFilterItems(ItemStack itemStack) {
		if (itemStack.hasTag() && itemStack.getOrCreateTag().getBoolean("Blacklist"))
			return new ItemStack[0];
		return ItemHelper.getNonEmptyStacks(getFilterItemHandler(itemStack)).toArray(ItemStack[]::new);
	}
}
