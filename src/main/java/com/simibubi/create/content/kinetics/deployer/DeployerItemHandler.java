package com.simibubi.create.content.kinetics.deployer;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class DeployerItemHandler implements IItemHandlerModifiable {

	private DeployerBlockEntity be;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerBlockEntity be) {
		this.be = be;
		this.player = be.player;
	}

	@Override
	public int getSlots() {
		return 1 + be.overflowItems.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= be.overflowItems.size() ? getHeld() : be.overflowItems.get(slot);
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (be.getLevel().isClientSide)
			return;
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		be.setChanged();
		be.sendData();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot < be.overflowItems.size())
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

		if (slot < be.overflowItems.size()) {
			ItemStack itemStack = be.overflowItems.get(slot);
			int toExtract = Math.min(amount, itemStack.getCount());
			ItemStack extracted = simulate ? itemStack.copy() : itemStack.split(toExtract);
			extracted.setCount(toExtract);
			if (!simulate && itemStack.isEmpty())
				be.overflowItems.remove(slot);
			if (!simulate && !extracted.isEmpty())
				be.setChanged();
			return extracted;
		}

		ItemStack held = getHeld();
		if (amount == 0 || held.isEmpty())
			return ItemStack.EMPTY;
		if (!be.filtering.getFilter()
			.isEmpty() && be.filtering.test(held))
			return ItemStack.EMPTY;
		if (simulate)
			return held.copy()
				.split(amount);

		ItemStack toReturn = held.split(amount);
		be.setChanged();
		be.sendData();
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getStackInSlot(slot).getMaxStackSize(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		FilteringBehaviour filteringBehaviour = be.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (slot < be.overflowItems.size()) {
			be.overflowItems.set(slot, stack);
			return;
		}
		set(stack);
	}

}
