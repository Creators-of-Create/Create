package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllContainerTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class AdjustableCrateContainer extends AbstractContainerMenu {

	public AdjustableCrateTileEntity te;
	public Inventory playerInventory;
	public boolean doubleCrate;

	public AdjustableCrateContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id);
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity tileEntity = world.getBlockEntity(extraData.readBlockPos());
		this.playerInventory = inv;
		if (tileEntity instanceof AdjustableCrateTileEntity) {
			this.te = (AdjustableCrateTileEntity) tileEntity;
			this.te.handleUpdateTag(te.getBlockState(), extraData.readNbt());
			init();
		}
	}

	public AdjustableCrateContainer(MenuType<?> type, int id, Inventory inv, AdjustableCrateTileEntity te) {
		super(type, id);
		this.te = te;
		this.playerInventory = inv;
		init();
	}

	public static AdjustableCrateContainer create(int id, Inventory inv, AdjustableCrateTileEntity te) {
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

		broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int crateSize = doubleCrate ? 32 : 16;
		if (index < crateSize) {
			moveItemStackTo(stack, crateSize, slots.size(), false);
			te.inventory.onContentsChanged(index);
		} else
			moveItemStackTo(stack, 0, crateSize - 1, false);

		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return te != null && te.canPlayerUse(player);
	}

}
