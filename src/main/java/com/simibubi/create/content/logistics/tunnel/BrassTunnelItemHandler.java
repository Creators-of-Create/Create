package com.simibubi.create.content.logistics.tunnel;

import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class BrassTunnelItemHandler implements IItemHandler {

	private BrassTunnelBlockEntity blockEntity;

	public BrassTunnelItemHandler(BrassTunnelBlockEntity be) {
		this.blockEntity = be;
	}
	
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return blockEntity.stackToDistribute;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!blockEntity.hasDistributionBehaviour()) {
			LazyOptional<IItemHandler> beltCapability = blockEntity.getBeltCapability();
			if (!beltCapability.isPresent())
				return stack;
			return beltCapability.orElse(null).insertItem(slot, stack, simulate);
		}
		
		if (!blockEntity.canTakeItems())
			return stack;
		
		ItemStack remainder = ItemHelper.limitCountToMaxStackSize(stack, simulate);
		if (!simulate) 
			blockEntity.setStackToDistribute(stack, null);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		LazyOptional<IItemHandler> beltCapability = blockEntity.getBeltCapability();
		if (!beltCapability.isPresent())
			return ItemStack.EMPTY;
		return beltCapability.orElse(null).extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return blockEntity.stackToDistribute.isEmpty() ? 64 : blockEntity.stackToDistribute.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

}
