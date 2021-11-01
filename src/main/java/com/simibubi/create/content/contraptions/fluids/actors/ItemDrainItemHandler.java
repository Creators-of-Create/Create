package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDrainItemHandler implements IItemHandler {

	private ItemDrainTileEntity te;
	private Direction side;

	public ItemDrainItemHandler(ItemDrainTileEntity te, Direction side) {
		this.te = te;
		this.side = side;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return te.getHeldItemStack();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!te.getHeldItemStack()
			.isEmpty())
			return stack;
		
		ItemStack returned = ItemStack.EMPTY;
		if (stack.getCount() > 1 && EmptyingByBasin.canItemBeEmptied(te.getLevel(), stack)) {
			returned = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
			stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
		}
		
		if (!simulate) {
			TransportedItemStack heldItem = new TransportedItemStack(stack);
			heldItem.prevBeltPosition = 0;
			te.setHeldItem(heldItem, side.getOpposite());
			te.notifyUpdate();
		}
		
		return returned;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		TransportedItemStack held = te.heldItem;
		if (held == null)
			return ItemStack.EMPTY;

		ItemStack stack = held.stack.copy();
		ItemStack extracted = stack.split(amount);
		if (!simulate) {
			te.heldItem.stack = stack;
			if (stack.isEmpty())
				te.heldItem = null;
			te.notifyUpdate();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

}
