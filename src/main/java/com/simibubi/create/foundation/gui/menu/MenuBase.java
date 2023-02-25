package com.simibubi.create.foundation.gui.menu;

import com.simibubi.create.foundation.utility.IInteractionChecker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class MenuBase<T> extends AbstractContainerMenu {

	public Player player;
	public Inventory playerInventory;
	public T contentHolder;

	protected MenuBase(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id);
		init(inv, createOnClient(extraData));
	}

	protected MenuBase(MenuType<?> type, int id, Inventory inv, T contentHolder) {
		super(type, id);
		init(inv, contentHolder);
	}

	protected void init(Inventory inv, T contentHolderIn) {
		player = inv.player;
		playerInventory = inv;
		contentHolder = contentHolderIn;
		initAndReadInventory(contentHolder);
		addSlots();
		broadcastChanges();
	}

	@OnlyIn(Dist.CLIENT)
	protected abstract T createOnClient(FriendlyByteBuf extraData);

	protected abstract void initAndReadInventory(T contentHolder);

	protected abstract void addSlots();

	protected abstract void saveData(T contentHolder);

	protected void addPlayerSlots(int x, int y) {
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		saveData(contentHolder);
	}

	@Override
	public boolean stillValid(Player player) {
		if (contentHolder == null)
			return false;
		if (contentHolder instanceof IInteractionChecker)
			return ((IInteractionChecker) contentHolder).canPlayerUse(player);
		return true;
	}

}
