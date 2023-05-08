package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDrainItemHandler implements IItemHandler {

	private ItemDrainBlockEntity blockEntity;
	private Direction side;

	public ItemDrainItemHandler(ItemDrainBlockEntity be, Direction side) {
		this.blockEntity = be;
		this.side = side;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return blockEntity.getHeldItemStack();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!blockEntity.getHeldItemStack()
			.isEmpty())
			return stack;
		
		ItemStack returned = ItemStack.EMPTY;
		if (stack.getCount() > 1 && EmptyingByBasin.canItemBeEmptied(blockEntity.getLevel(), stack)) {
			returned = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
			stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
		}
		
		if (!simulate) {
			TransportedItemStack heldItem = new TransportedItemStack(stack);
			heldItem.prevBeltPosition = 0;
			blockEntity.setHeldItem(heldItem, side.getOpposite());
			blockEntity.notifyUpdate();
		}
		
		return returned;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		TransportedItemStack held = blockEntity.heldItem;
		if (held == null)
			return ItemStack.EMPTY;

		ItemStack stack = held.stack.copy();
		ItemStack extracted = stack.split(amount);
		if (!simulate) {
			blockEntity.heldItem.stack = stack;
			if (stack.isEmpty())
				blockEntity.heldItem = null;
			blockEntity.notifyUpdate();
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
