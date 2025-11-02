package com.simibubi.create.content.logistics.filter;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PackageFilterItem extends FilterItem {
	protected PackageFilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public List<Component> makeSummary(ItemStack filter) {
		String address = PackageItem.getAddress(filter);
		if (address.isBlank())
			return Collections.emptyList();

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
	public DataComponentType<?> getComponentType() {
		return AllDataComponents.PACKAGE_ADDRESS;
	}

	@Override
	public FilterItemStack makeStackWrapper(ItemStack filter) {
		return new PackageFilterItemStack(filter);
	}

	@Override
	public ItemStack[] getFilterItems(ItemStack stack) {
		return new ItemStack[0];
	}
}
