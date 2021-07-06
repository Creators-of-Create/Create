package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.SlotItemHandler;

public class SchematicannonContainer extends Container {

	private SchematicannonTileEntity te;
	private PlayerEntity player;

	public SchematicannonContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer buffer) {
		super(type, id);
		player = inv.player;
		ClientWorld world = Minecraft.getInstance().world;
		TileEntity tileEntity = world.getTileEntity(buffer.readBlockPos());
		if (tileEntity instanceof SchematicannonTileEntity) {
			this.te = (SchematicannonTileEntity) tileEntity;
			this.te.handleUpdateTag(te.getBlockState(), buffer.readCompoundTag());
			init();
		}
	}

	public SchematicannonContainer(ContainerType<?> type, int id, PlayerInventory inv, SchematicannonTileEntity te) {
		super(type, id);
		player = inv.player;
		this.te = te;
		init();
	}

	public static SchematicannonContainer create(int id, PlayerInventory inv, SchematicannonTileEntity te) {
		return new SchematicannonContainer(AllContainerTypes.SCHEMATICANNON.get(), id, inv, te);
	}

	protected void init() {
		int x = 0;
		int y = 0;

		addSlot(new SlotItemHandler(te.inventory, 0, x + 15, y + 65));
		addSlot(new SlotItemHandler(te.inventory, 1, x + 171, y + 65));
		addSlot(new SlotItemHandler(te.inventory, 2, x + 134, y + 19));
		addSlot(new SlotItemHandler(te.inventory, 3, x + 174, y + 19));
		addSlot(new SlotItemHandler(te.inventory, 4, x + 15, y + 19));

		int invX = 37;
		int invY = 161;

		// player Slots
		for (int row = 0; row < 3; ++row) 
			for (int col = 0; col < 9; ++col) 
				addSlot(new Slot(player.inventory, col + row * 9 + 9, invX + col * 18, invY + row * 18));
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			addSlot(new Slot(player.inventory, hotbarSlot, invX + hotbarSlot * 18, invY + 58));

		detectAndSendChanges();
	}

	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return te != null && te.canPlayerUse(player);
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
