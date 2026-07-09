package com.simibubi.create.foundation.fluid;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class LegacyFluidHandlerItemAdapter extends LegacyFluidHandlerAdapter implements IFluidHandlerItem {
	private final ItemStack container;

	public LegacyFluidHandlerItemAdapter(ResourceHandler<FluidResource> handler, ItemStack container) {
		super(handler);
		this.container = container;
	}

	public static IFluidHandlerItem of(ItemStack stack) {
		if (stack.isEmpty())
			return null;
		ItemAccess access = ItemAccess.forStack(stack);
		ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? null : new LegacyFluidHandlerItemAdapter(handler, stack);
	}

	@Override
	public ItemStack getContainer() {
		return container;
	}
}
