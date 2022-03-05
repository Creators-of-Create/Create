package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandlerModifiable;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class DeployerItemHandler implements IItemHandlerModifiable {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerTileEntity te) {
		this.te = te;
		this.player = te.player;
	}

	@Override
	public int getSlots() {
		return 1 + te.overflowItems.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= te.overflowItems.size() ? getHeld() : te.overflowItems.get(slot);
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (te.getLevel().isClientSide)
			return;
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		te.setChanged();
		te.sendData();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot < te.overflowItems.size())
			return stack;
		if (!isItemValid(slot, stack))
			return stack;

		ItemStack held = getHeld();
		if (held.isEmpty()) {
			if (!simulate)
				set(stack);
			return ItemStack.EMPTY;
		}

		if (!ItemHandlerHelper.canItemStacksStack(held, stack))
			return stack;

		int space = held.getMaxStackSize() - held.getCount();
		ItemStack remainder = stack.copy();
		ItemStack split = remainder.split(space);

		if (space == 0)
			return stack;
		if (!simulate) {
			held = held.copy();
			held.setCount(held.getCount() + split.getCount());
			set(held);
		}

		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;

		if (slot < te.overflowItems.size()) {
			ItemStack itemStack = te.overflowItems.get(slot);
			int toExtract = Math.min(amount, itemStack.getCount());
			ItemStack extracted = simulate ? itemStack.copy() : itemStack.split(toExtract);
			extracted.setCount(toExtract);
			if (!simulate && itemStack.isEmpty())
				te.overflowItems.remove(slot);
			if (!simulate && !extracted.isEmpty())
				te.setChanged();
			return extracted;
		}

		ItemStack held = getHeld();
		if (amount == 0 || held.isEmpty())
			return ItemStack.EMPTY;
		if (!te.filtering.getFilter()
			.isEmpty() && te.filtering.test(held))
			return ItemStack.EMPTY;
		if (simulate)
			return held.copy()
				.split(amount);

		ItemStack toReturn = held.split(amount);
		te.setChanged();
		te.sendData();
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getStackInSlot(slot).getMaxStackSize(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		FilteringBehaviour filteringBehaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (slot < te.overflowItems.size()) {
			te.overflowItems.set(slot, stack);
			return;
		}
		set(stack);
	}

}
