package com.simibubi.create.foundation.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ContainerBase<T> extends Container {

	public PlayerEntity player;
	public PlayerInventory playerInventory;
	public T contentHolder;

	protected ContainerBase(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id);
		init(inv, createOnClient(extraData));
	}

	protected ContainerBase(ContainerType<?> type, int id, PlayerInventory inv, T contentHolder) {
		super(type, id);
		init(inv, contentHolder);
	}

	protected void init(PlayerInventory inv, T contentHolderIn) {
		player = inv.player;
		playerInventory = inv;
		contentHolder = contentHolderIn;
		initAndReadInventory(contentHolder);
		addSlots();
		broadcastChanges();
	}

	@OnlyIn(Dist.CLIENT)
	protected abstract T createOnClient(PacketBuffer extraData);

	protected abstract void addSlots();

	protected abstract void initAndReadInventory(T contentHolder);

	protected abstract void saveData(T contentHolder);

	protected void addPlayerSlots(int x, int y) {
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	@Override
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		saveData(contentHolder);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		if (contentHolder == null)
			return false;
		if (contentHolder instanceof IInteractionChecker)
			return ((IInteractionChecker) contentHolder).canPlayerUse(player);
		return true;
	}

}
