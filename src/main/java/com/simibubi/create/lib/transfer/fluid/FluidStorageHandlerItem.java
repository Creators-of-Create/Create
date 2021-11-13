package com.simibubi.create.lib.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class FluidStorageHandlerItem extends FluidStorageHandler implements IFluidHandlerItem {
	protected final ItemStack stack;

	public FluidStorageHandlerItem(ItemStack stack, Storage<FluidVariant> storage) {
		super(storage);
		this.stack = stack;
	}

	@Override
	public ItemStack getContainer() {
		return stack;
	}
}
