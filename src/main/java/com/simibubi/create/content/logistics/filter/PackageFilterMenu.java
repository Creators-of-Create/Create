package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllMenuTypes;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PackageFilterMenu extends AbstractFilterMenu {

	String address;
	EditBox addressInput;

	public PackageFilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public PackageFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
		super(type, id, inv, stack);
	}

	public static PackageFilterMenu create(int id, Inventory inv, ItemStack stack) {
		return new PackageFilterMenu(AllMenuTypes.PACKAGE_FILTER.get(), id, inv, stack);
	}

	@Override
	protected int getPlayerInventoryXOffset() {
		return 40;
	}

	@Override
	protected int getPlayerInventoryYOffset() {
		return 101;
	}

	@Override
	protected void addFilterSlots() {}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler();
	}

	@Override
	public void clearContents() {
		address = "";
	}

	@Override
	protected void initAndReadInventory(ItemStack filterItem) {
		super.initAndReadInventory(filterItem);
		address = filterItem.getOrDefault(AllDataComponents.PACKAGE_ADDRESS, "");
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		super.saveData(filterItem);
		if (address.isBlank())
			filterItem.remove(AllDataComponents.PACKAGE_ADDRESS);
		else
			filterItem.set(AllDataComponents.PACKAGE_ADDRESS, address);
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

}
