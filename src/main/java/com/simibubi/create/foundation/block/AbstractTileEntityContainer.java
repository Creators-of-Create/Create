package com.simibubi.create.foundation.block;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractTileEntityContainer<T extends TileEntity> extends Container {

	public T te;
	public PlayerEntity player;

	@SuppressWarnings("unchecked")
	public AbstractTileEntityContainer(AllContainerTypes containerType, int id, PlayerInventory inv,
			PacketBuffer extraData) {
		super(containerType.type, id);
		ClientWorld world = Minecraft.getInstance().world;
		this.te = (T) world.getTileEntity(extraData.readBlockPos());
		this.te.handleUpdateTag(extraData.readCompoundTag());
		this.player = inv.player;
		init();
	}

	public AbstractTileEntityContainer(AllContainerTypes containerType, int id, PlayerInventory inv, T te) {
		super(containerType.type, id);
		this.te = te;
		this.player = inv.player;
		init();
	}

	protected abstract void init();

	protected void addPlayerSlots(int x, int y) {
		for (int row = 0; row < 3; ++row) 
			for (int col = 0; col < 9; ++col) 
				this.addSlot(new Slot(player.inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) 
			this.addSlot(new Slot(player.inventory, hotbarSlot, x + hotbarSlot * 18, y + 3 * 18 + 4));
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		return ItemStack.EMPTY;
	}

}
