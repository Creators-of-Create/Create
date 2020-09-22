package com.simibubi.create.content.schematics.block;

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
import net.minecraftforge.items.SlotItemHandler;

public class SchematicannonContainer extends Container {

	private SchematicannonTileEntity te;
	private PlayerEntity player;

	public SchematicannonContainer(int id, PlayerInventory inv, PacketBuffer buffer) {
		super(AllContainerTypes.SCHEMATICANNON.type, id);
		player = inv.player;
		ClientWorld world = Minecraft.getInstance().world;
		TileEntity tileEntity = world.getTileEntity(buffer.readBlockPos());
		if (tileEntity instanceof SchematicannonTileEntity) {
			this.te = (SchematicannonTileEntity) tileEntity;
			this.te.handleUpdateTag(te.getBlockState(), buffer.readCompoundTag());
			init();
		}
	}

	public SchematicannonContainer(int id, PlayerInventory inv, SchematicannonTileEntity te) {
		super(AllContainerTypes.SCHEMATICANNON.type, id);
		player = inv.player;
		this.te = te;
		init();
	}

	protected void init() {
		int x = 20;
		int y = 0;

		addSlot(new SlotItemHandler(te.inventory, 0, x + 14, y + 37));
		addSlot(new SlotItemHandler(te.inventory, 1, x + 170, y + 37));
		addSlot(new SlotItemHandler(te.inventory, 2, x + 222, y + 21));
		addSlot(new SlotItemHandler(te.inventory, 3, x + 222, y + 60));
		addSlot(new SlotItemHandler(te.inventory, 4, x + 51, y + 135));

		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				addSlot(new Slot(player.inventory, col + row * 9 + 9, -2 + col * 18, 163 + row * 18));
			}
		}
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			addSlot(new Slot(player.inventory, hotbarSlot, -2 + hotbarSlot * 18, 221));
		}

		detectAndSendChanges();
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
	}

	public SchematicannonTileEntity getTileEntity() {
		return te;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.getHasStack())
			return ItemStack.EMPTY;
		ItemStack stack = clickedSlot.getStack();

		if (index < 5) {
			mergeItemStack(stack, 5, inventorySlots.size(), false);
		} else {
			if (mergeItemStack(stack, 0, 1, false) || mergeItemStack(stack, 2, 3, false)
					|| mergeItemStack(stack, 4, 5, false))
				;
		}

		return ItemStack.EMPTY;
	}

}
