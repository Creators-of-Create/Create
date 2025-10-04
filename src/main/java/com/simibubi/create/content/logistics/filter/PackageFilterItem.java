package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class PackageFilterItem extends FilterItem{
	protected PackageFilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public List<Component> makeSummary(ItemStack filter) {
		if (!filter.hasTag()) return Collections.emptyList();

		String address = filter.getOrCreateTag()
			.getString("Address");
		if (address.isBlank()) return Collections.emptyList();

		return List.of(CreateLang.text("-> ")
			.style(ChatFormatting.GRAY)
			.add(CreateLang.text(address)
				.style(ChatFormatting.GOLD))
			.component());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return PackageFilterMenu.create(id, inv, player.getMainHandItem());
	}

	@Override
	public FilterItemStack makeStackWrapper(ItemStack filter) {
		return new PackageFilterItemStack(filter);
	}

	@Override
	public ItemStack[] getFilterItems(ItemStack itemStack) {
		return new ItemStack[0];
	}
}
