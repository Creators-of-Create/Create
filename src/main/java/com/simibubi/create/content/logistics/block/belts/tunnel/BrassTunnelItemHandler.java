package com.simibubi.create.content.logistics.block.belts.tunnel;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class BrassTunnelItemHandler implements IItemHandler {

	private BrassTunnelTileEntity te;

	public BrassTunnelItemHandler(BrassTunnelTileEntity te) {
		this.te = te;
	}
	
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return te.stackToDistribute;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!te.hasDistributionBehaviour()) {
			LazyOptional<IItemHandler> beltCapability = te.getBeltCapability();
			if (!beltCapability.isPresent())
				return stack;
			return beltCapability.orElse(null).insertItem(slot, stack, simulate);
		}
		
		if (!te.canTakeItems())
			return stack;
		if (!simulate) 
			te.setStackToDistribute(stack, null);
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		LazyOptional<IItemHandler> beltCapability = te.getBeltCapability();
		if (!beltCapability.isPresent())
			return ItemStack.EMPTY;
		return beltCapability.orElse(null).extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return te.stackToDistribute.isEmpty() ? 64 : te.stackToDistribute.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

}
