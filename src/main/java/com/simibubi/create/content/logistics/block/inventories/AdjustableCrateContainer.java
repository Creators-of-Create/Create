package com.simibubi.create.content.logistics.block.inventories;

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

public class AdjustableCrateContainer extends Container {

	public AdjustableCrateTileEntity te;
	public PlayerInventory playerInventory;
	public boolean doubleCrate;

	public AdjustableCrateContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id);
		ClientWorld world = Minecraft.getInstance().world;
		TileEntity tileEntity = world.getTileEntity(extraData.readBlockPos());
		this.playerInventory = inv;
		if (tileEntity instanceof AdjustableCrateTileEntity) {
			this.te = (AdjustableCrateTileEntity) tileEntity;
			this.te.handleUpdateTag(te.getBlockState(), extraData.readCompoundTag());
			init();
		}
	}

	public AdjustableCrateContainer(ContainerType<?> type, int id, PlayerInventory inv, AdjustableCrateTileEntity te) {
		super(type, id);
		this.te = te;
		this.playerInventory = inv;
		init();
	}

	public static AdjustableCrateContainer create(int id, PlayerInventory inv, AdjustableCrateTileEntity te) {
		return new AdjustableCrateContainer(AllContainerTypes.FLEXCRATE.get(), id, inv, te);
	}

	private void init() {
		doubleCrate = te.isDoubleCrate();
		int x = doubleCrate ? 23 : 53;
		int maxCol = doubleCrate ? 8 : 4;
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < maxCol; ++col) {
				this.addSlot(new SlotItemHandler(te.inventory, col + row * maxCol, x + col * 18, 20 + row * 18));
			}
		}

		// player Slots
		int xOffset = doubleCrate ? 20 : 8;
		int yOffset = 149;
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(playerInventory, hotbarSlot, xOffset + hotbarSlot * 18, yOffset + 58));
		}

		detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.getHasStack())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getStack();
		int crateSize = doubleCrate ? 32 : 16;
		if (index < crateSize) {
			mergeItemStack(stack, crateSize, inventorySlots.size(), false);
			te.inventory.onContentsChanged(index);
		} else
			mergeItemStack(stack, 0, crateSize - 1, false);

		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

}
