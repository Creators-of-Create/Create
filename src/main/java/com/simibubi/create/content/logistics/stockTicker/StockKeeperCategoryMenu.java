package com.simibubi.create.content.logistics.stockTicker;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.gui.menu.MenuBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class StockKeeperCategoryMenu extends MenuBase<StockTickerBlockEntity> {

	public boolean slotsActive = true;
	public ItemStackHandler proxyInventory;

	public StockKeeperCategoryMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public static AbstractContainerMenu create(int pContainerId, Inventory pPlayerInventory,
		StockTickerBlockEntity stockTickerBlockEntity) {
		return new StockKeeperCategoryMenu(AllMenuTypes.STOCK_KEEPER_CATEGORY.get(), pContainerId, pPlayerInventory,
			stockTickerBlockEntity);
	}

	public StockKeeperCategoryMenu(MenuType<?> type, int id, Inventory inv, StockTickerBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {
		proxyInventory = new ItemStackHandler(1);
	}

	@Override
	protected StockTickerBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
		BlockPos blockPos = extraData.readBlockPos();
		return AllBlocks.STOCK_TICKER.get()
			.getBlockEntity(Minecraft.getInstance().level, blockPos);
	}

	@Override
	protected void addSlots() {
		addSlot(new InactiveItemHandlerSlot(proxyInventory, 0, 16, 24));
		addPlayerSlots(18, 106);
	}

	@Override
	protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
		return new InactiveSlot(inventory, index, x, y);
	}

	@Override
	protected void saveData(StockTickerBlockEntity contentHolder) {}

	@Override
	public boolean stillValid(Player player) {
		return !contentHolder.isRemoved() && player.position()
			.closerThan(Vec3.atCenterOf(contentHolder.getBlockPos()), player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 4);
	}

	class InactiveSlot extends Slot {

		public InactiveSlot(Container pContainer, int pIndex, int pX, int pY) {
			super(pContainer, pIndex, pX, pY);
		}

		@Override
		public boolean isActive() {
			return slotsActive;
		}

	}

	class InactiveItemHandlerSlot extends SlotItemHandler {

		public InactiveItemHandlerSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack) {
			return super.mayPlace(stack) && (stack.isEmpty() || stack.getItem() instanceof FilterItem);
		}

		@Override
		public boolean isActive() {
			return slotsActive;
		}

	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = 1;
		boolean success = false;
		if (index < size) {
			success = !moveItemStackTo(stack, size, slots.size(), true);
		} else
			success = !moveItemStackTo(stack, 0, size, false);

		return success ? ItemStack.EMPTY : stack;
	}

}
