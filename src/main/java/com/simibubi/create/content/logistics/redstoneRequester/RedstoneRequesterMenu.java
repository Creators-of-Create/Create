package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RedstoneRequesterMenu extends GhostItemMenu<RedstoneRequesterBlockEntity> {

	public RedstoneRequesterMenu(MenuType<?> type, int id, Inventory inv, RedstoneRequesterBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}

	public RedstoneRequesterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public static RedstoneRequesterMenu create(int id, Inventory inv, RedstoneRequesterBlockEntity be) {
		return new RedstoneRequesterMenu(AllMenuTypes.REDSTONE_REQUESTER.get(), id, inv, be);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		ItemStackHandler inventory = new ItemStackHandler(9);
		List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
		for (int i = 0; i < stacks.size(); i++)
			inventory.setStackInSlot(i, stacks.get(i).stack.copyWithCount(1));
		return inventory;
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected RedstoneRequesterBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
		BlockPos blockPos = extraData.readBlockPos();
		return AllBlocks.REDSTONE_REQUESTER.get()
			.getBlockEntity(Minecraft.getInstance().level, blockPos);
	}

	@Override
	protected void addSlots() {
		int playerX = 5;
		int playerY = 142;
		int slotX = 27;
		int slotY = 28;

		addPlayerSlots(playerX, playerY);
		for (int i = 0; i < 9; i++)
			addSlot(new SorterProofSlot(ghostInventory, i, slotX + 20 * i, slotY));
	}

	@Override
	protected void saveData(RedstoneRequesterBlockEntity contentHolder) {
		List<BigItemStack> stacks = contentHolder.encodedRequest.stacks();
		ArrayList<BigItemStack> list = new ArrayList<>();
		for (int i = 0; i < ghostInventory.getSlots(); i++) {
			ItemStack stackInSlot = ghostInventory.getStackInSlot(i);
			if (stackInSlot.isEmpty())
				continue;
			list.add(new BigItemStack(stackInSlot.copyWithCount(1), i < stacks.size() ? stacks.get(i).count : 1));
		}

		PackageOrderWithCrafts newRequest = new PackageOrderWithCrafts(new PackageOrder(list), contentHolder.encodedRequest.orderedCrafts());
		if (!newRequest.orderedStacksMatchOrderedRecipes())
			newRequest = PackageOrderWithCrafts.simple(newRequest.stacks());
		contentHolder.encodedRequest = newRequest;
		contentHolder.sendData();
	}

	// this is used to prevent InventorySorter from interfering with scrolling on the slots.
	// we just need a class to use as a marker, see InventorySorterCompat
	public static class SorterProofSlot extends SlotItemHandler {
		public SorterProofSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
			super(itemHandler, index, xPosition, yPosition);
		}
	}
}
