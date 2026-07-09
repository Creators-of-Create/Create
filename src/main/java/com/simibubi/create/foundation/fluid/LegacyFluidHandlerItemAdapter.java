package com.simibubi.create.foundation.fluid;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public class LegacyFluidHandlerItemAdapter extends LegacyFluidHandlerAdapter implements IFluidHandlerItem {
	private final ItemAccess access;

	public LegacyFluidHandlerItemAdapter(ResourceHandler<FluidResource> handler, ItemAccess access) {
		super(handler);
		this.access = access;
	}

	public static IFluidHandlerItem of(ItemStack stack) {
		if (stack.isEmpty())
			return null;
		ResourceHandler<ItemResource> itemHandler = VanillaContainerWrapper.of(new SimpleContainer(stack));
		ItemAccess access = ItemAccess.forHandlerIndexStrict(itemHandler, 0);
		ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? null : new LegacyFluidHandlerItemAdapter(handler, access);
	}

	@Override
	public ItemStack getContainer() {
		ItemResource resource = access.getResource();
		return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(access.getAmount());
	}
}
