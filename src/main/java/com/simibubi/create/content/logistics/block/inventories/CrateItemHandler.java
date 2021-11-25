package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllContraptionStorages;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;
import com.simibubi.create.api.contraption.ContraptionItemStackHandler;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CrateItemHandler extends ContraptionItemStackHandler {
	private int allowedAmount = 0;

	public CrateItemHandler() {
		super(32);
	}

	public CrateItemHandler(int allowedAmount) {
		this();
		this.allowedAmount = allowedAmount;
	}

	public int getAllowedAmount() {
		return allowedAmount;
	}

	public CrateItemHandler setAllowedAmount(int allowedAmount) {
		this.allowedAmount = allowedAmount;
		return this;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected ContraptionStorageRegistry registry() {
		return AllContraptionStorages.ADJUSTABLE_CRATE.get();
	}

	@Override
	public int getSlotLimit(int slot) {
		if (slot < getAllowedAmount() / 64)
			return super.getSlotLimit(slot);
		else if (slot == getAllowedAmount() / 64)
			return getAllowedAmount() % 64;
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if (slot > allowedAmount / 64)
			return false;
		return super.isItemValid(slot, stack);
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		nbt.putInt("AllowedAmount", getAllowedAmount());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		allowedAmount = nbt.getInt("AllowedAmount");
	}
}
