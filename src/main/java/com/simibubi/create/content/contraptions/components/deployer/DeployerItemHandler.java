package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class DeployerItemHandler implements IItemHandlerModifiable {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerTileEntity te) {
		this.te = te;
		this.player = te.player;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getHeld();
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getHeldItemMainhand();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (te.getWorld().isRemote)
			return;
		player.setHeldItem(Hand.MAIN_HAND, stack);
		te.markDirty();
		te.sendData();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack held = getHeld();
		if (!isItemValid(slot, stack))
			return stack;
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
		ItemStack held = getHeld();
		if (amount == 0 || held.isEmpty())
			return ItemStack.EMPTY;
		if (simulate)
			return held.copy().split(amount);

		ItemStack toReturn = held.split(amount);
		te.markDirty();
		te.sendData();
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getHeld().getMaxStackSize(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get(te, FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		set(stack);
	}

}
